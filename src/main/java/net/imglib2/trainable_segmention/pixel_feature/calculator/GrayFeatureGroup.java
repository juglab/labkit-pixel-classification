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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class GrayFeatureGroup extends AbstractFeatureGroup {

	GrayFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		super(ops, settings);
		if(settings.globals().imageType() != GlobalSettings.ImageType.GRAY_SCALE)
			throw new IllegalArgumentException("GrayFeatureGroup requires ImageType to be gray scale");
	}

	@Override
	public void apply(RandomAccessible<?> in, List<RandomAccessibleInterval<FloatType>> out) {
		if(!(in.randomAccess().get() instanceof RealType))
			throw new IllegalArgumentException();
		joiner.apply(RevampUtils.randomAccessibleToFloat(RevampUtils.uncheckedCast(in)), out);
	}

	@Override
	public Class<FloatType> getType() {
		return FloatType.class;
	}
}
