
package net.imglib2.trainable_segmention.classification;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imagej.ops.OpEnvironment;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imagej.ops.special.hybrid.UnaryHybridCF;
import net.imglib2.*;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJMultiChannelImage;
import net.imglib2.trainable_segmention.clij_random_forest.RandomForestPrediction;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.GenericComposite;
import preview.net.imglib2.loops.LoopBuilder;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class Segmenter {

	private final FeatureCalculator features;

	private final List<String> classNames;

	public Classifier getClassifier() {
		return classifier;
	}

	private weka.classifiers.Classifier classifier;

	private boolean useGpu = false;

	private Segmenter(List<String> classNames, FeatureCalculator features,
		Classifier classifier)
	{
		this.classNames = Collections.unmodifiableList(classNames);
		this.features = Objects.requireNonNull(features);
		this.classifier = Objects.requireNonNull(classifier);
	}

	public Segmenter(OpEnvironment ops, List<String> classNames, FeatureSettings features,
		Classifier classifier)
	{
		this(classNames, new FeatureCalculator(ops, features), classifier);
	}

	public void setUseGpu(boolean useGpu) {
		this.useGpu = useGpu;
		features().setUseGPU(useGpu);
	}

	public FeatureCalculator features() {
		return features;
	}

	public FeatureSettings settings() {
		return features.settings();
	}

	public RandomAccessibleInterval<UnsignedByteType> segment(RandomAccessibleInterval<?> image) {
		return segment(image, new UnsignedByteType());
	}

	private <T extends NativeType<T>> RandomAccessibleInterval<T> createImage(T type,
		Interval interval)
	{
		// FIXME: use this in all places where it's useful.
		long[] size = Intervals.dimensionsAsLongArray(interval);
		long[] min = Intervals.minAsLongArray(interval);
		Img<T> img = new ArrayImgFactory<>(type).create(size);
		return Views.translate(img, min);
	}

	public <T extends IntegerType<T> & NativeType<T>> RandomAccessibleInterval<T> segment(
		RandomAccessibleInterval<?> image, T type)
	{
		Interval interval = features.outputIntervalFromInput(image);
		RandomAccessibleInterval<T> rai = createImage(type, interval);
		segment(rai, Views.extendBorder(image));
		return rai;
	}

	public void segment(RandomAccessibleInterval<? extends IntegerType<?>> out,
		RandomAccessible<?> image)
	{
		Objects.requireNonNull(out);
		Objects.requireNonNull(image);
		if (useGpu)
			segmentGpu(image, out);
		else
			segmentCpu(image, out);
	}

	private void segmentCpu(RandomAccessible<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> out)
	{
		RandomAccessibleInterval<FloatType> featureValues = features.apply(image, out);
		LoopBuilder.setImages(Views.collapseReal(featureValues), out).multiThreaded().forEachChunk(
			chunk -> {
				chunk.forEachPixel(pixelClassificationOp()::compute);
				return null;
			});
	}

	private void segmentGpu(RandomAccessible<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> out)
	{
		RandomForestPrediction prediction = new RandomForestPrediction(Cast.unchecked(classifier),
			classNames.size(), features.count());
		CLIJ2 clij = CLIJ2.getInstance();
		try (
			CLIJMultiChannelImage featureStack = features.applyUseGpu(image, out);
			ClearCLBuffer segmentationBuffer = prediction.segment(clij, featureStack))
		{
			CLIJCopy.copyToRai(segmentationBuffer, out);
		}
	}

	public RandomAccessibleInterval<? extends RealType<?>> predict(
		RandomAccessibleInterval<?> image)
	{
		Objects.requireNonNull(image);
		Interval outputInterval = features.outputIntervalFromInput(image);
		RandomAccessibleInterval<FloatType> result = RevampUtils.createImage(RevampUtils
			.appendDimensionToInterval(
				outputInterval, 0, classNames.size() - 1), new FloatType());
		predict(result, Views.extendBorder(image));
		return result;
	}

	public void predict(RandomAccessibleInterval<? extends RealType<?>> out,
		RandomAccessible<?> image)
	{
		Objects.requireNonNull(out);
		Objects.requireNonNull(image);
		if (useGpu)
			predictGpu(out, image);
		else
			predictCpu(out, image);
	}

	private void predictCpu(RandomAccessibleInterval<? extends RealType<?>> out,
		RandomAccessible<?> image)
	{
		Interval interval = RevampUtils.removeLastDimension(out);
		RandomAccessibleInterval<FloatType> featureValues = features.apply(image, interval);
		LoopBuilder.setImages(Views.collapseReal(featureValues), Views.collapse(out)).multiThreaded()
			.forEachChunk(
				chunk -> {
					chunk.forEachPixel(pixelPredictionOp()::compute);
					return null;
				});
	}

	private void predictGpu(RandomAccessibleInterval<? extends RealType<?>> out,
		RandomAccessible<?> image)
	{
		CLIJ2 clij = CLIJ2.getInstance();
		Interval interval = RevampUtils.removeLastDimension(out);
		RandomForestPrediction prediction = new RandomForestPrediction(Cast.unchecked(classifier),
			classNames.size(), features.count());
		try (
			CLIJMultiChannelImage featureStack = features.applyUseGpu(image, interval);
			CLIJMultiChannelImage distribution = new CLIJMultiChannelImage(clij, featureStack
				.getSpatialDimensions(), features.count()))
		{
			prediction.distribution(clij, featureStack, distribution);
			featureStack.copyTo(out);
		}
	}

	private UnaryHybridCF<Composite<? extends RealType<?>>, Composite<? extends RealType<?>>>
		pixelPredictionOp()
	{
		return new PixelPredictionOp();
	}

	private UnaryHybridCF<Composite<? extends RealType<?>>, IntegerType<?>> pixelClassificationOp() {
		return new PixelClassifierOp();
	}

	public List<String> classNames() {
		return classNames;
	}

	public Training training() {
		return new MyTrainingData();
	}

	public JsonElement toJsonTree() {
		JsonObject json = new JsonObject();
		json.add("features", features.settings().toJson());
		json.add("classNames", new Gson().toJsonTree(classNames));
		json.add("classifier", ClassifierSerialization.wekaToJson(classifier));
		return json;
	}

	public static Segmenter fromJson(OpEnvironment ops, JsonElement json) {
		JsonObject object = json.getAsJsonObject();
		return new Segmenter(
			ops,
			new Gson().fromJson(object.get("classNames"), new TypeToken<List<String>>()
			{}.getType()),
			FeatureSettings.fromJson(object.get("features")),
			ClassifierSerialization.jsonToWeka(object.get("classifier")));
	}

	private class MyTrainingData implements Training {

		final Instances instances;

		final int featureCount;

		MyTrainingData() {
			this.instances = new Instances("segment", new ArrayList<>(attributes()), 1);
			this.featureCount = features.count();
			instances.setClassIndex(featureCount);
		}

		@Override
		public void add(Composite<? extends RealType<?>> featureVector, int classIndex) {
			instances.add(RevampUtils.getInstance(featureCount, classIndex, featureVector));
		}

		@Override
		public void train() {
			RevampUtils.wrapException(() -> classifier.buildClassifier(instances));

		}
	}

	// -- Helper methods --

	private Attribute[] attributesAsArray() {
		List<Attribute> attributes = attributes();
		return attributes.toArray(new Attribute[attributes.size()]);
	}

	private List<Attribute> attributes() {
		Stream<Attribute> featureAttributes = features.attributeLabels().stream().map(Attribute::new);
		Stream<Attribute> classAttribute = Stream.of(new Attribute("class", classNames));
		return Stream.concat(featureAttributes, classAttribute).collect(Collectors.toList());
	}

	// -- Helper classes --

	private class PixelClassifierOp extends
		AbstractUnaryHybridCF<Composite<? extends RealType<?>>, IntegerType<?>>
	{

		CompositeInstance compositeInstance = new CompositeInstance(null, attributesAsArray());

		@Override
		public UnaryHybridCF<Composite<? extends RealType<?>>, IntegerType<?>>
			getIndependentInstance()
		{
			return new PixelClassifierOp();
		}

		@Override
		public IntegerType<?> createOutput(Composite<? extends RealType<?>> input) {
			return new UnsignedByteType();
		}

		@Override
		public void compute(Composite<? extends RealType<?>> input, IntegerType<?> output) {
			compositeInstance.setSource(input);
			RevampUtils.wrapException(() -> output.setInteger((int) classifier.classifyInstance(
				compositeInstance)));
		}

	}

	private class PixelPredictionOp extends
		AbstractUnaryHybridCF<Composite<? extends RealType<?>>, Composite<? extends RealType<?>>>
	{

		CompositeInstance compositeInstance = new CompositeInstance(null, attributesAsArray());

		@Override
		public UnaryHybridCF<Composite<? extends RealType<?>>, Composite<? extends RealType<?>>>
			getIndependentInstance()
		{
			return new PixelPredictionOp();
		}

		@Override
		public void compute(Composite<? extends RealType<?>> input,
			Composite<? extends RealType<?>> output)
		{
			compositeInstance.setSource(input);
			double[] result = RevampUtils.wrapException(() -> classifier.distributionForInstance(
				compositeInstance));
			for (int i = 0, n = result.length; i < n; i++)
				output.get(i).setReal(result[i]);
		}

		@Override
		public Composite<? extends RealType<?>> createOutput(Composite<? extends RealType<?>> input) {
			return new GenericComposite<>(ArrayImgs.doubles(compositeInstance.numClasses())
				.randomAccess());
		}
	}
}
