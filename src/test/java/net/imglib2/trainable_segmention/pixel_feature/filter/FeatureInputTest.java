package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.DerivedNormalDistribution;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class FeatureInputTest {

	private RandomAccessibleInterval<FloatType> image = initImage();

	private Interval target = Intervals.createMinSize(10, 10, 10, 10, 10, 10);

	private FeatureInput cache = new FeatureInput(image, target);

	private RandomAccessibleInterval<FloatType> initImage() {
		RandomAccessibleInterval<FloatType> result = ArrayImgs.floats(30, 30, 30);
		Views.interval(result, Intervals.createMinSize(15, 15, 15, 15, 15, 15)).forEach(RealType::setOne);
		return result;
	}

	@Test
	public void testSource() {
		assertSame(image, cache.original());
	}

	@Test
	public void testGauss() {
		RandomAccessibleInterval<? extends RealType<?>> gauss = cache.gauss(2.0);
		RandomAccessibleInterval<DoubleType> expected = create(target);
		Gauss3.gauss(2.0, image, expected);
		Utils.assertImagesEqual(expected, (RandomAccessibleInterval) gauss);
	}

	@Test
	public void testGaussCache() {
		assertSame(cache.gauss(1.0), cache.gauss(1.0));
		assertNotSame(cache.gauss(2.0), cache.gauss(1.0));
	}

	@Test
	public void testDerivedGauss() {
		RandomAccessibleInterval<? extends RealType<?>>	result = cache.derivedGauss(2.0, 0, 1, 0);
		RandomAccessibleInterval<DoubleType> expected = deriveY();
		Utils.assertImagesEqual(expected, (RandomAccessibleInterval) result);
	}

	@Test
	public void testDerivedGaussCache() {
		assertSame(cache.derivedGauss(2.0, 1, 0, 0), cache.derivedGauss(2.0, 1, 0, 0))	;
		assertNotSame(cache.derivedGauss(2.0, 1, 0, 0), cache.derivedGauss(2.0, 0, 0, 0))	;
		assertNotSame(cache.derivedGauss(3.0, 1, 0, 0), cache.derivedGauss(2.0, 1, 0, 0))	;
		assertNotSame(cache.derivedGauss(2.0, 1, 0, 0), cache.derivedGauss(2.0, 1, 0))	;
	}

	private RandomAccessibleInterval<DoubleType> deriveY() {
		RandomAccessibleInterval<DoubleType> expected = create(target);
		Kernel1D[] kernels = {
				DerivedNormalDistribution.derivedGaussKernel(2.0, 0),
				DerivedNormalDistribution.derivedGaussKernel(2.0, 1),
				DerivedNormalDistribution.derivedGaussKernel(2.0, 0),
		};
		SeparableKernelConvolution.convolve(kernels, image, expected);
		return expected;
	}

	private RandomAccessibleInterval<DoubleType> create(Interval target) {
		Img<DoubleType> result = ArrayImgs.doubles(Intervals.dimensionsAsLongArray(target));
		return Views.translate(result, Intervals.minAsLongArray(target));
	}
}
