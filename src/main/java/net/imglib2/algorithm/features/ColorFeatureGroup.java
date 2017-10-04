package net.imglib2.algorithm.features;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class ColorFeatureGroup implements FeatureGroup {

	private final FeatureJoiner joiner;

	private final FeatureSettings settings;

	ColorFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		this.settings = settings;
		List<FeatureOp> featuresOps = settings.features().stream().map(x -> x.newInstance(ops, settings.globals())).collect(Collectors.toList());
		this.joiner = new FeatureJoiner(featuresOps);
		if(settings.globals().imageType() != GlobalSettings.ImageType.COLOR)
			throw new IllegalArgumentException("ColorFeatureGroup requires GlobalSettings.imageType() to be COLOR.");
	}

	@Override
	public OpEnvironment ops() {
		return joiner.ops();
	}

	@Override
	public int count() {
		return joiner.count() * channelCount();
	}

	@Override
	public void apply(RandomAccessible<?> input, List<RandomAccessibleInterval<FloatType>> output) {
		List<RandomAccessible<FloatType>> inputs = RevampUtils.splitChannels(RevampUtils.castRandomAccessible(input, ARGBType.class));
		List<List<RandomAccessibleInterval<FloatType>>> outputs = split(output, channelCount());
		for (int i = 0; i < channelCount(); i++)
			joiner.apply(inputs.get(i), outputs.get(i));
	}

	@Override
	public List<String> attributeLabels() {
		return prepend(joiner.globalSettings().imageType().channelNames(), joiner.attributeLabels());
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
	public Class<?> getType() {
		return ARGBType.class;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getType(), attributeLabels());
	}

	// -- Helper methods --

	private int channelCount() {
		return 3;
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
