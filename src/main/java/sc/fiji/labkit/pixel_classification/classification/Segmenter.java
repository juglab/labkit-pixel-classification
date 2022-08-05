/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.classification;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import hr.irb.fastRandomForest.FastRandomForest;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.*;
import net.imglib2.img.array.ArrayImgFactory;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuCopy;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPool;
import sc.fiji.labkit.pixel_classification.random_forest.CpuRandomForestPrediction;
import sc.fiji.labkit.pixel_classification.gpu.random_forest.GpuRandomForestPrediction;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.RevampUtils;
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

	private GpuRandomForestPrediction gpuPrediction;

	private CpuRandomForestPrediction cpuPrediction;

	private boolean useGpu = false;

	private Segmenter(List<String> classNames, FeatureCalculator features,
		Classifier classifier)
	{
		this.classNames = Collections.unmodifiableList(classNames);
		this.features = Objects.requireNonNull(features);
		this.classifier = Objects.requireNonNull(classifier);
		updatePrecacheRandomForests();
	}

	private void updatePrecacheRandomForests()
	{
		this.gpuPrediction = new GpuRandomForestPrediction( Cast.unchecked( classifier ), features.count() );
		this.cpuPrediction = new CpuRandomForestPrediction( Cast.unchecked( classifier ), features.count() );
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
		cpuPrediction.segment(featureValues, out);
	}

	private void segmentGpu(RandomAccessible<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> out)
	{
		try (GpuApi scope = GpuPool.borrowGpu()) {
			GpuImage featureStack = features.applyUseGpu(scope, image, out);
			GpuImage segmentationBuffer = gpuPrediction.segment(scope, featureStack);
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
		cpuPrediction.distribution(featureValues, out);
	}

	private void predictGpu(RandomAccessibleInterval<? extends RealType<?>> out,
		RandomAccessible<?> image)
	{
		Interval interval = RevampUtils.removeLastDimension(out);
		try (GpuApi scope = GpuPool.borrowGpu()) {
			GpuImage featureStack = features.applyUseGpu(scope, image, interval);
			GpuImage distribution = scope.create(featureStack.getDimensions(), classNames.size(),
				NativeTypeEnum.Float);
			gpuPrediction.distribution(scope, featureStack, distribution);
			GpuCopy.copyFromTo(distribution, out);
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
			updatePrecacheRandomForests();
		}
	}

	// -- Helper methods --

	private List<Attribute> attributes() {
		Stream<Attribute> featureAttributes = features.attributeLabels().stream().map(Attribute::new);
		Stream<Attribute> classAttribute = Stream.of(new Attribute("class", classNames));
		return Stream.concat(featureAttributes, classAttribute).collect(Collectors.toList());
	}
}
