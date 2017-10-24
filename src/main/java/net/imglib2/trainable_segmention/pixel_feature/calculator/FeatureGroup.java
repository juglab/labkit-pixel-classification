package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpEnvironment;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureJoiner;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FeatureGroup {

	private final FeatureJoiner joiner;

	private final FeatureSettings settings;

	private final InputPreprocessor preprocessor;

	public FeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		this.settings = settings;
		List<FeatureOp> featureOps = settings.features().stream()
				.map(x -> x.newInstance(ops, settings.globals())).collect(Collectors.toList());
		this.joiner = new FeatureJoiner(featureOps);
		this.preprocessor = initPreprocessor(settings.globals().imageType());
	}

	private InputPreprocessor initPreprocessor(GlobalSettings.ImageType imageType) {
		switch (imageType) {
			case COLOR:
				return new ColorInputPreprocessor();
			case GRAY_SCALE:
				return new GrayInputPreprocessor();
			default:
				throw new UnsupportedOperationException("Unsupported ImageType: " + settings().globals().imageType());
		}
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

	public Class<?> getType() {
		return preprocessor.getType();
	}

	public int count() {
		return joiner.count() * channelCount();
	}

	public List<String> attributeLabels() {
		return prepend(settings.globals().imageType().channelNames(), joiner.attributeLabels());
	}

	public void apply(RandomAccessible<?> input, List<RandomAccessibleInterval<FloatType>> output) {
		if(!getType().isInstance(input.randomAccess().get()))
			throw new IllegalArgumentException();
		List<RandomAccessible<FloatType>> channels = preprocessor.getChannels(input);
		List<List<RandomAccessibleInterval<FloatType>>> outputs = split(output, channels.size());
		for (int i = 0; i < settings().globals().imageType().channelCount(); i++)
			joiner.apply(channels.get(i), outputs.get(i));
	}

	public RandomAccessibleInterval<FloatType> apply(RandomAccessibleInterval<?> image) {
		return apply(Views.extendBorder(image), image);
	}

	public RandomAccessibleInterval<FloatType> apply(RandomAccessible<?> extendedImage, Interval interval) {
		Img<FloatType> result = ops().create().img(RevampUtils.extend(interval, 0, count() - 1), new FloatType());
		apply(extendedImage, RevampUtils.slices(result));
		return result;
	}


	// -- Helper methods --

	private int channelCount() {
		return settings.globals().imageType().channelCount();
	}

	private static List<String> prepend(List<String> prepend, List<String> labels) {
		return labels.stream()
				.flatMap(label -> prepend.stream().map(pre -> pre.isEmpty() ? label : pre + "_" + label))
				.collect(Collectors.toList());
	}

	private static <T> List<List<T>> split(List<T> input, int count) {
		return IntStream.range(0, count).mapToObj(
				i -> filterByIndexPredicate(input, index -> index % count == i)
		).collect(Collectors.toList());
	}

	private static <T> List<T> filterByIndexPredicate(List<T> in, IntPredicate predicate) {
		return IntStream.range(0, in.size()).filter(predicate).mapToObj(in::get).collect(Collectors.toList());
	}
}
