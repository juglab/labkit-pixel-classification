package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
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
public class ColorFeatureGroup extends AbstractFeatureGroup {

	ColorFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		super(ops, settings);
		if(settings.globals().imageType() != GlobalSettings.ImageType.COLOR)
			throw new IllegalArgumentException("ColorFeatureGroup requires GlobalSettings.imageType() to be COLOR.");
	}

	@Override
	public void apply(RandomAccessible<?> input, List<RandomAccessibleInterval<FloatType>> output) {
		List<RandomAccessible<FloatType>> inputs = RevampUtils.splitChannels(RevampUtils.castRandomAccessible(input, ARGBType.class));
		List<List<RandomAccessibleInterval<FloatType>>> outputs = split(output, settings().globals().imageType().channelCount());
		for (int i = 0; i < settings().globals().imageType().channelCount(); i++)
			joiner.apply(inputs.get(i), outputs.get(i));
	}

	@Override
	public Class<?> getType() {
		return ARGBType.class;
	}

	// -- Helper methods --

	private static <T> List<List<T>> split(List<T> input, int count) {
		return IntStream.range(0, count).mapToObj(
				i -> filterByIndexPredicate(input, index -> index % count == i)
		).collect(Collectors.toList());
	}

	private static <T> List<T> filterByIndexPredicate(List<T> in, IntPredicate predicate) {
		return IntStream.range(0, in.size()).filter(predicate).mapToObj(in::get).collect(Collectors.toList());
	}
}
