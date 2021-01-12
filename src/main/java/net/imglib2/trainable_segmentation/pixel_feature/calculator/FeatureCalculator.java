
package net.imglib2.trainable_segmentation.pixel_feature.calculator;

import net.imglib2.trainable_segmentation.gpu.api.GpuApi;
import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmentation.RevampUtils;
import net.imglib2.trainable_segmentation.gpu.api.GpuCopy;
import net.imglib2.trainable_segmentation.gpu.GpuFeatureInput;
import net.imglib2.trainable_segmentation.gpu.api.GpuPool;
import net.imglib2.trainable_segmentation.gpu.api.GpuView;
import net.imglib2.trainable_segmentation.gpu.api.GpuViews;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureJoiner;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
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

	private boolean useGpu = false;

	public FeatureCalculator(Context context, FeatureSettings settings) {
		this.settings = settings;
		List<FeatureOp> featureOps = settings.features().stream()
			.map(x -> x.newInstance(context, settings.globals())).collect(Collectors.toList());
		this.joiner = new FeatureJoiner(featureOps);
		this.preprocessor = initPreprocessor(settings.globals().channelSetting());
	}

	public static FeatureCalculator.Builder default2d() {
		return new Builder().dimensions(2);
	}

	private InputPreprocessor initPreprocessor(ChannelSetting channelSetting) {
		if (ChannelSetting.RGB.equals(channelSetting))
			return new ColorInputPreprocessor(settings.globals());
		if (ChannelSetting.DEPRECATED_RGB.equals(channelSetting))
			return new DeprecatedColorInputPreprocessor(settings.globals());
		if (ChannelSetting.SINGLE.equals(channelSetting))
			return new GrayInputPreprocessor(settings.globals());
		if (channelSetting.isMultiple())
			return new MultiChannelInputPreprocessor(settings.globals());
		throw new UnsupportedOperationException("Unsupported channel setting: " + settings().globals()
			.channelSetting());
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

	public void setUseGpu(boolean useGpu) {
		this.useGpu = useGpu;
	}

	/**
	 * TODO what channel order? XYZC
	 */
	public void apply(RandomAccessible<?> input, RandomAccessibleInterval<FloatType> output) {
		if (useGpu) {
			Interval interval = RevampUtils.removeLastDimension(output);
			try (GpuApi scope = GpuPool.borrowGpu()) {
				GpuImage result = applyUseGpu(scope, input, interval);
				GpuCopy.copyFromTo(result, output);
			}
		}
		else {
			applyUseCpu(input, output);
		}
	}

	public RandomAccessibleInterval<FloatType> apply(RandomAccessibleInterval<?> image) {
		return apply(Views.extendBorder(image), preprocessor.outputIntervalFromInput(image));
	}

	public RandomAccessibleInterval<FloatType> apply(RandomAccessible<?> extendedImage,
		Interval interval)
	{
		FinalInterval fullInterval = Intervals.addDimension(interval, 0, count() - 1);
		if (useGpu) {
			try (GpuApi scope = GpuPool.borrowGpu()) {
				GpuImage featureStack = applyUseGpu(scope, extendedImage, interval);
				return Views.translate(scope.pullRAIMultiChannel(featureStack),
					Intervals.minAsLongArray(fullInterval));
			}
		}
		else {
			Img<FloatType> image = ArrayImgs.floats(Intervals.dimensionsAsLongArray(fullInterval));
			IntervalView<FloatType> rai = Views.translate(image, Intervals.minAsLongArray(fullInterval));
			applyUseCpu(extendedImage, rai);
			return rai;
		}
	}

	private void applyUseCpu(RandomAccessible<?> input, RandomAccessibleInterval<FloatType> output) {
		List<RandomAccessible<FloatType>> channels = preprocessor.getChannels(input);
		List<List<RandomAccessibleInterval<FloatType>>> outputs = split(RevampUtils.slices(output),
			channels.size());
		double[] pixelSize = settings.globals().pixelSizeAsDoubleArray();
		for (int i = 0; i < channels.size(); i++) {
			FeatureInput in = new FeatureInput(channels.get(i), outputs.get(i).get(0), pixelSize);
			joiner.apply(in, outputs.get(i));
		}
	}

	public GpuImage applyUseGpu(GpuApi gpu, RandomAccessible<?> input, Interval interval) {
		if (interval.numDimensions() != settings().globals().numDimensions())
			throw new IllegalArgumentException("Wrong dimension of the output interval.");
		double[] pixelSize = settings.globals().pixelSizeAsDoubleArray();
		List<RandomAccessible<FloatType>> channels = preprocessor.getChannels(input);
		GpuImage featureStack = gpu.create(Intervals.dimensionsAsLongArray(interval), count(),
			NativeTypeEnum.Float);
		List<List<GpuView>> outputs = split(GpuViews.channels(featureStack), channels.size());
		for (int i = 0; i < channels.size(); i++) {
			try (GpuApi scope = gpu.subScope()) {
				GpuFeatureInput in = new GpuFeatureInput(scope, channels.get(i), interval, pixelSize);
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

		private Context context;

		private final List<FeatureSetting> features = new ArrayList<>();

		private Builder() {
			super();
		}

		public Builder context(Context context) {
			this.context = context;
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
			if (context == null)
				context = SingletonContext.getInstance();
			GlobalSettings globalSettings = buildGlobalSettings();
			FeatureSettings featureSettings = new FeatureSettings(globalSettings, features);
			return new FeatureCalculator(context, featureSettings);
		}
	}
}
