package net.imglib2.algorithm.features.classification;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.imagej.ops.OpEnvironment;
import net.imagej.ops.Ops;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imagej.ops.special.hybrid.UnaryHybridCF;
import net.imglib2.*;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.algorithm.features.gson.FeaturesGson;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.GenericComposite;
import weka.core.Attribute;
import weka.core.Instances;

import java.util.*;

/**
 * @author Matthias Arzt
 */
public class Segmenter {

	private final FeatureGroup features;

	private final List<String> classNames;

	private weka.classifiers.Classifier classifier;

	private boolean isTrained = false;

	private final OpEnvironment ops;

	public Segmenter(OpEnvironment ops, List<String> classNames, FeatureGroup features, weka.classifiers.Classifier classifier) {
		this.ops = Objects.requireNonNull(ops);
		this.classNames = Collections.unmodifiableList(classNames);
		this.features = Objects.requireNonNull(features);
		this.classifier = Objects.requireNonNull(classifier);
	}

	public FeatureGroup features() {
		return features;
	}

	public Img<ByteType> segment(RandomAccessibleInterval<?> image) {
		return segment(image, new ByteType());
	}

	public <T extends IntegerType<T> & NativeType<T>> Img<T> segment(RandomAccessibleInterval<?> image, T type) {
		Img<T> result = ops.create().img(image, type);
		segment(result, Views.extendBorder(image));
		return result;
	}

	public void segment(RandomAccessibleInterval<? extends IntegerType<?>> out, RandomAccessible<?> image) {
		RandomAccessibleInterval<FloatType> featureValues = Features.applyOnImg(features, image, out);
		ops.run(Ops.Map.class, out, Views.collapseReal(featureValues), pixelClassificationOp());
	}

	public RandomAccessibleInterval<? extends IntegerType<?>> segmentLazyOnComposite(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> featureValues) {
		return Views.interval(new MappingView<>(featureValues, pixelClassificationOp()), featureValues);
	}

	public RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> predict(RandomAccessibleInterval<FloatType> image) {
		Img<FloatType> img = ops.create().img(RevampUtils.extend(image, 0, classNames.size()), new FloatType());
		RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> collapsed = Views.collapse(img);
		predict(collapsed, Views.extendBorder(image));
		return collapsed;
	}

	public void predict(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> out, RandomAccessible<FloatType> image) {
		RandomAccessibleInterval<FloatType> featureValues = Features.applyOnImg(features, image, out);
		ops.run(Ops.Map.class, out, Views.collapseReal(featureValues), pixelPredictionOp());
	}

	public RandomAccessibleInterval<Composite<? extends RealType<?>>> predictLazyOnComposite(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> featureValues) {
		return Views.interval(new MappingView<>(featureValues, pixelPredictionOp()), featureValues);
	}

	public UnaryHybridCF<Composite<? extends RealType<?>>, Composite<? extends RealType<?>>> pixelPredictionOp() {
		return new PixelPredictionOp();
	}

	public UnaryHybridCF<Composite<? extends RealType<?>>, IntegerType<?>> pixelClassificationOp() {
		return new PixelClassifierOp();
	}

	public List<String> classNames() {
		return classNames;
	}

	public Training training() {
		return new MyTrainingData();
	}

	public boolean isTrained() {
		return isTrained;
	}

	public JsonElement toJsonTree() {
		JsonObject json = new JsonObject();
		json.add("features", FeaturesGson.toJsonTree(features));
		json.add("classNames", new Gson().toJsonTree(classNames));
		json.add("classifier", ClassifierSerialization.wekaToJson(classifier));
		return json;
	}

	public static Segmenter fromJson(OpEnvironment ops, JsonElement json) {
		JsonObject object = json.getAsJsonObject();
		return new Segmenter(
				ops,
				new Gson().fromJson(object.get("classNames"), new TypeToken<List<String>>() {}.getType()),
				FeaturesGson.fromJson(ops, object.get("features")),
				ClassifierSerialization.jsonToWeka(object.get("classifier"))
		);
	}

	private class MyTrainingData implements Training {

		final Instances instances;

		final int featureCount;

		MyTrainingData() {
			this.instances = new Instances("segment", new ArrayList<>(Features.attributes(features, classNames)), 1);
			this.featureCount = features.count();
			instances.setClassIndex(featureCount);
		}

		@Override
		public void add(Composite<? extends RealType<?>> featureVector, int classIndex) {
			instances.add(RevampUtils.getInstance(featureCount, classIndex, featureVector));
		}

		@Override
		public void train() {
			RevampUtils.wrapException( () ->
 				classifier.buildClassifier(instances)
			);

		}
	}

	// -- Helper methods --

	Attribute[] attributesAsArray() {
		List<Attribute> attributes = Features.attributes(features, classNames);
		return attributes.toArray(new Attribute[attributes.size()]);
	}

	// -- Helper classes --

	private class PixelClassifierOp extends AbstractUnaryHybridCF<Composite<? extends RealType<?>>, IntegerType<?>> {

		CompositeInstance compositeInstance = new CompositeInstance(null, attributesAsArray());

		@Override
		public UnaryHybridCF<Composite<? extends RealType<?>>, IntegerType<?>> getIndependentInstance() {
			return new PixelClassifierOp();
		}

		@Override
		public IntegerType<?> createOutput(Composite<? extends RealType<?>> input) {
			return new ByteType();
		}

		@Override
		public void compute(Composite<? extends RealType<?>> input, IntegerType<?> output) {
			compositeInstance.setSource(input);
			RevampUtils.wrapException(() -> output.setInteger((int) classifier.classifyInstance(compositeInstance)));
		}

	}

	private class PixelPredictionOp extends AbstractUnaryHybridCF<Composite<? extends RealType<?>>, Composite<? extends RealType<?>>> {

		CompositeInstance compositeInstance = new CompositeInstance(null, attributesAsArray());

		@Override
		public UnaryHybridCF<Composite<? extends RealType<?>>, Composite<? extends RealType<?>>> getIndependentInstance() {
			return new PixelPredictionOp();
		}

		@Override
		public void compute(Composite<? extends RealType<?>> input, Composite<? extends RealType<?>> output) {
			compositeInstance.setSource(input);
			double[] result = RevampUtils.wrapException(() -> classifier.distributionForInstance(compositeInstance));
			for (int i = 0, n = result.length; i < n; i++)
				output.get(i).setReal(result[i]);
		}

		@Override
		public Composite<? extends RealType<?>> createOutput(Composite<? extends RealType<?>> input) {
			return new GenericComposite<>(ArrayImgs.doubles(compositeInstance.numClasses()).randomAccess());
		}
	}
}
