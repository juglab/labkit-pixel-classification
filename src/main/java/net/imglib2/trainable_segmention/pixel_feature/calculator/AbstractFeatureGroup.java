package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureJoiner;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

abstract class AbstractFeatureGroup implements FeatureGroup {

	protected final FeatureJoiner joiner;

	protected final FeatureSettings settings;

	protected AbstractFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		this.settings = settings;
		List<FeatureOp> featureOps = settings.features().stream()
				.map(x -> x.newInstance(ops, settings.globals())).collect(Collectors.toList());
		this.joiner = new FeatureJoiner(featureOps);
		checkImageType(settings);
	}

	private void checkImageType(FeatureSettings settings) {
		GlobalSettings.ImageType expected = getImageType();
		if(settings.globals().imageType() != expected)
			throw new IllegalArgumentException("ColorFeatureGroup requires GlobalSettings.imageType() to be " + expected + ".");
	}

	protected abstract GlobalSettings.ImageType getImageType();

	@Override
	public OpEnvironment ops() {
		return joiner.ops();
	}

	@Override
	public FeatureSettings settings() {
		return settings;
	}

	@Override
	public List<FeatureOp> features() {
		return joiner.features();
	}

	@Override
	public int count() {
		return joiner.count() * channelCount();
	}

	@Override
	public List<String> attributeLabels() {
		return prepend(settings.globals().imageType().channelNames(), joiner.attributeLabels());
	}

	@Override
	public void apply(RandomAccessible<?> input, List<RandomAccessibleInterval<FloatType>> output) {
		if(!getType().isInstance(input.randomAccess().get()))
			throw new IllegalArgumentException();
		List<RandomAccessible<FloatType>> channels = getChannels(input);
		List<List<RandomAccessibleInterval<FloatType>>> outputs = split(output, channels.size());
		for (int i = 0; i < settings().globals().imageType().channelCount(); i++)
			joiner.apply(channels.get(i), outputs.get(i));
	}

	protected abstract List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input);

	// TODO: Why calculate hash code? Should this method be moved to FeatureSettings
	@Override
	public int hashCode() {
		return Objects.hash(getType(), attributeLabels());
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
