package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class ColorFeatureGroup extends AbstractFeatureGroup {

	ColorFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		super(ops, settings);
	}

	@Override
	protected GlobalSettings.ImageType getImageType() {
		return GlobalSettings.ImageType.COLOR;
	}

	@Override
	protected List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input) {
		return RevampUtils.splitChannels(RevampUtils.castRandomAccessible(input, ARGBType.class));
	}

	@Override
	public Class<?> getType() {
		return ARGBType.class;
	}
}
