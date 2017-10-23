package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractFeatureOp
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>
		implements FeatureOp
{
	@Parameter
	private GlobalSettings globalSettings;

	@Override
	public RandomAccessibleInterval<FloatType> calculate(RandomAccessibleInterval<FloatType> input) {
		Img<FloatType> output = ops().create().img(RevampUtils.extend(input, 0, count() - 1), new FloatType());
		apply(input, RevampUtils.slices(output));
		return output;
	}

	@Override
	public GlobalSettings globalSettings() {
		return globalSettings;
	}
}
