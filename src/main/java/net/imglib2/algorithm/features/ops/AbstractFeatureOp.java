package net.imglib2.algorithm.features.ops;

import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractFeatureOp
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>
		implements FeatureOp
{

	@Override
	public RandomAccessibleInterval<FloatType> calculate(RandomAccessibleInterval<FloatType> input) {
		Img<FloatType> output = ops().create().img(RevampUtils.extend(input, 0, count() - 1), new FloatType());
		apply(input, RevampUtils.slices(output));
		return output;
	}
}
