
package net.imglib2.trainable_segmentation.classification;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.imglib2.trainable_segmentation.gpu.api.GpuApi;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.*;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.trainable_segmentation.gpu.api.GpuCopy;
import net.imglib2.trainable_segmentation.gpu.api.GpuPool;
import net.imglib2.trainable_segmentation.gpu.random_forest.CpuRandomForestPrediction;
import net.imglib2.trainable_segmentation.gpu.random_forest.GpuRandomForestPrediction;
import net.imglib2.trainable_segmentation.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmentation.RevampUtils;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.Context;
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

	private final weka.classifiers.Classifier classifier;

	private GpuRandomForestPrediction predicition;

	private boolean useGpu = false;

	private Segmenter(List<String> classNames, FeatureCalculator features,
		Classifier classifier)
	{
		this.classNames = Collections.unmodifiableList(classNames);
		this.features = Objects.requireNonNull(features);
		this.classifier = Objects.requireNonNull(classifier);
		this.predicition = new GpuRandomForestPrediction(Cast.unchecked(classifier),
			features.count());
	}

	public Segmenter(Context context, List<String> classNames, FeatureSettings features,
		Classifier classifier)
	{
		this(classNames, new FeatureCalculator(context, features), classifier);
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setUseGpu(boolean useGpu) {
		this.useGpu = useGpu;
		features.setUseGpu(useGpu);
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
		CpuRandomForestPrediction forest = new CpuRandomForestPrediction((FastRandomForest) classifier,
			features.count());
		forest.segment(featureValues, out);
	}

	private void segmentGpu(RandomAccessible<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> out)
	{
		try (GpuApi scope = GpuPool.borrowGpu()) {
			GpuRandomForestPrediction
					prediction = new GpuRandomForestPrediction(Cast.unchecked(classifier),
				features.count());
			GpuImage featureStack = features.applyUseGpu(scope, image, out);
			GpuImage segmentationBuffer = prediction.segment(scope, featureStack);
			GpuCopy.copyFromTo(segmentationBuffer, out);
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
		CpuRandomForestPrediction prediction = new CpuRandomForestPrediction(Cast.unchecked(classifier),
			features.count());
		prediction.distribution(featureValues, out);
	}

	private void predictGpu(RandomAccessibleInterval<? extends RealType<?>> out,
		RandomAccessible<?> image)
	{
		Interval interval = RevampUtils.removeLastDimension(out);
		GpuRandomForestPrediction prediction = new GpuRandomForestPrediction(Cast.unchecked(classifier),
			features.count());
		try (GpuApi scope = GpuPool.borrowGpu()) {
			GpuImage featureStack = features.applyUseGpu(scope, image, interval);
			GpuImage distribution = scope.create(featureStack.getDimensions(), features.count(),
				NativeTypeEnum.Float);
			prediction.distribution(scope, featureStack, distribution);
			GpuCopy.copyFromTo(featureStack, out);
		}
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

	public static Segmenter fromJson(Context context, JsonElement json) {
		JsonObject object = json.getAsJsonObject();
		return new Segmenter(
			context,
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
			predicition = new GpuRandomForestPrediction(Cast.unchecked(classifier), features.count());
		}
	}

	// -- Helper methods --

	private List<Attribute> attributes() {
		Stream<Attribute> featureAttributes = features.attributeLabels().stream().map(Attribute::new);
		Stream<Attribute> classAttribute = Stream.of(new Attribute("class", classNames));
		return Stream.concat(featureAttributes, classAttribute).collect(Collectors.toList());
	}
}
