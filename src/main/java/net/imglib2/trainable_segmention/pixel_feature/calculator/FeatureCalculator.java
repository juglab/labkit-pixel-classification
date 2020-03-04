
package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.haesleinhuepf.clij2.CLIJ2;
import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJMultiChannelImage;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureJoiner;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FeatureCalculator {

	private final FeatureJoiner joiner;

	private final FeatureSettings settings;

	private final InputPreprocessor preprocessor;

	private final CLIJ2 clij = CLIJ2.getInstance();

	public FeatureCalculator(OpEnvironment ops, FeatureSettings settings) {
		this.settings = settings;
		List<FeatureOp> featureOps = settings.features().stream()
			.map(x -> x.newInstance(ops, settings.globals())).collect(Collectors.toList());
		this.joiner = new FeatureJoiner(featureOps);
		this.preprocessor = initPreprocessor(settings.globals().channelSetting());
	}

	public static FeatureCalculator.Builder default2d() {
		return new Builder();
	}

	private InputPreprocessor initPreprocessor(ChannelSetting channelSetting) {
		if (ChannelSetting.RGB.equals(channelSetting))
			return new ColorInputPreprocessor(settings.globals());
		if (ChannelSetting.SINGLE.equals(channelSetting))
			return new GrayInputPreprocessor(settings.globals());
		if (channelSetting.isMultiple())
			return new MultiChannelInputPreprocessor(settings.globals());
		throw new UnsupportedOperationException("Unsupported channel setting: " + settings().globals()
			.channelSetting());
	}

	public OpEnvironment ops() {
		return joiner.ops();
	}

	public FeatureSettings settings() {
		return settings;
	}

	public List<FeatureOp> features() {
		return joiner.features();
	}

	public int count() {
		return joiner.count() * channelCount();
	}

	public List<String> attributeLabels() {
		return prepend(settings.globals().channelSetting().channels(), joiner.attributeLabels());
	}

	/**
	 * TODO what channel order? XYZC
	 */
	public void apply(RandomAccessible<?> input, List<RandomAccessibleInterval<FloatType>> output) {
		RandomAccessibleInterval<FloatType> interval = output.get(0);
		try( CLIJMultiChannelImage result = applyWithCLIJ(input, interval) ) {
			result.copyTo(Views.stack(output));
		}
	}

	public RandomAccessibleInterval<FloatType> apply(RandomAccessibleInterval<?> image) {
		return apply(Views.extendBorder(image), preprocessor.outputIntervalFromInput(image));
	}

	public RandomAccessibleInterval<FloatType> apply(RandomAccessible<?> extendedImage,
		Interval interval)
	{
		return applyWithCLIJ(extendedImage, interval).asRAI();
	}

	public CLIJMultiChannelImage applyWithCLIJ(RandomAccessible<?> input, Interval interval) {
		if (interval.numDimensions() != settings().globals().numDimensions())
			throw new IllegalArgumentException("Wrong dimension of the output interval.");
		double[] pixelSize = settings.globals().pixelSizeAsDoubleArray();
		List<RandomAccessible<FloatType>> channels = preprocessor.getChannels(input);
		CLIJMultiChannelImage featureStack = new CLIJMultiChannelImage(clij, Intervals.dimensionsAsLongArray(interval), joiner.count());
		List<List<CLIJView>> outputs = split(featureStack.channels(), channels.size());
		for (int i = 0; i < channels.size(); i++) {
			try( CLIJFeatureInput in = new CLIJFeatureInput(clij, channels.get(i), interval, pixelSize) ) {
				joiner.prefetch(in);
				joiner.apply(in, outputs.get(i));
			}
		}
		return featureStack;
	}

	public Interval outputIntervalFromInput(RandomAccessibleInterval<?> image) {
		return preprocessor.outputIntervalFromInput(image);
	}

	// -- Helper methods --

	private int channelCount() {
		return settings.globals().channelSetting().channels().size();
	}

	private static List<String> prepend(List<String> prepend, List<String> labels) {
		return labels.stream()
			.flatMap(label -> prepend.stream().map(pre -> pre.isEmpty() ? label : pre + "_" + label))
			.collect(Collectors.toList());
	}

	private static <T> List<List<T>> split(List<T> input, int count) {
		return IntStream.range(0, count).mapToObj(
			i -> filterByIndexPredicate(input, index -> index % count == i)).collect(Collectors.toList());
	}

	private static <T> List<T> filterByIndexPredicate(List<T> in, IntPredicate predicate) {
		return IntStream.range(0, in.size()).filter(predicate).mapToObj(in::get).collect(Collectors
			.toList());
	}

	public static class Builder extends GlobalSettings.AbstractBuilder<Builder> {

		private OpEnvironment ops;

		private final List<FeatureSetting> features = new ArrayList<>();

		private Builder() {}

		public Builder ops(OpEnvironment ops) {
			this.ops = ops;
			return this;
		}

		public Builder addFeatures(FeatureSetting... features) {
			this.features.addAll(Arrays.asList(features));
			return this;
		}

		public Builder addFeature(Class<? extends FeatureOp> clazz, Object... parameters) {
			this.features.add(new FeatureSetting(clazz, parameters));
			return this;
		}

		public Builder addFeature(FeatureSetting featureSetting) {
			addFeatures(featureSetting);
			return this;
		}

		public FeatureCalculator build() {
			if (ops == null)
				ops = new Context().service(OpService.class);
			GlobalSettings globalSettings = buildGlobalSettings();
			FeatureSettings featureSettings = new FeatureSettings(globalSettings, features);
			return new FeatureCalculator(ops, featureSettings);
		}
	}
}
