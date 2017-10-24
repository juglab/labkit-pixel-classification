package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureJoiner;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class GrayFeatureGroup extends AbstractFeatureGroup {

	GrayFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		super(ops, settings);
	}

	@Override
	protected GlobalSettings.ImageType getImageType() {
		return GlobalSettings.ImageType.GRAY_SCALE;
	}

	@Override
	protected List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input) {
		if(!(input.randomAccess().get() instanceof RealType))
			throw new IllegalArgumentException();
		return Collections.singletonList(RevampUtils.randomAccessibleToFloat(RevampUtils.uncheckedCast(input)));
	}

	@Override
	public Class<?> getType() {
		return RealType.class;
	}
}
