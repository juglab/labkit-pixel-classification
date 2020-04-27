
package preview.net.imglib2.algorithm.gauss3;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.Test;
import preview.net.imglib2.algorithm.convolution.fast_gauss.FastGauss;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import preview.net.imglib2.loops.LoopBuilder;

import java.awt.GraphicsEnvironment;
import java.util.function.BiFunction;

import static org.junit.Assume.assumeFalse;

public class Gauss3Test<T extends RealType<T> & NativeType<T>> {

	private T type = Cast.unchecked(new DoubleType());
	private double sigma = 4;
	private long width = 100;
	private long center = width / 2;
	private RandomAccessibleInterval<T> input = scaleAndAddOffset(dirac());
	private RandomAccessibleInterval<T> expected = scaleAndAddOffset(idealGaussian(sigma));

	@Test
	public void testGauss3() {
		RandomAccessibleInterval<T> result = createEmptyImage();
		Gauss3.gauss(sigma, Views.extendBorder(input), result);
		Utils.assertImagesEqual(60, subtractOffset(expected), subtractOffset(result));
		Utils.assertImagesEqual(55, deriveX(expected), deriveX(result));
		Utils.assertImagesEqual(45, secondDerivativeX(expected), secondDerivativeX(result));
	}

	@Test
	public void testFastGauss() {
		RandomAccessibleInterval<T> result = createEmptyImage();
		FastGauss.convolve(sigma, Views.extendBorder(input), result);
		Utils.assertImagesEqual(60, subtractOffset(expected), subtractOffset(result));
	}

	@Test
	public void testFastGaussWithoutOffset() {
		RandomAccessibleInterval<T> result = createEmptyImage();
		FastGauss.convolve(sigma, Views.extendBorder(subtractOffset(input)), result);
		Utils.assertImagesEqual(70, subtractOffset(expected), result);
	}

	@Test
	public void testIJ1Gauss() throws InterruptedException {
		assumeFalse(GraphicsEnvironment.isHeadless());
		ImagePlus imp = ImageJFunctions.wrap(input, "").duplicate();
		IJ.run(imp, "Gaussian Blur...", "sigma=" + sigma);
		Img<FloatType> result = ImageJFunctions.convertFloat(imp);
		Utils.assertImagesEqual(75, subtractOffset(expected), subtractOffset(result));
		Utils.assertImagesEqual(75, deriveX(expected), deriveX(result));
		Utils.assertImagesEqual(65, secondDerivativeX(expected), secondDerivativeX(result));
	}

	// -- Helper methods --

	private RandomAccessibleInterval<T> subtractOffset(
		RandomAccessibleInterval<? extends RealType<?>> image)
	{
		return Converters.convert(image, (i, o) -> o.setReal(i.getRealDouble() - 80), type);
	}

	private RandomAccessibleInterval<T> scaleAndAddOffset(RandomAccessibleInterval<T> dirac) {
		LoopBuilder.setImages(dirac).forEachPixel(pixel -> pixel.setReal(20 * pixel.getRealDouble() +
			80));
		return dirac;
	}

	private RandomAccessibleInterval<T> idealGaussian(double sigma) {
		return createImage((x, y) -> gauss(sigma, x) * gauss(sigma, y));
	}

	private double gauss(double sigma, double x) {
		double a = 1. / Math.sqrt(2 * Math.PI * Math.pow(sigma, 2));
		double b = -0.5 / Math.pow(sigma, 2);
		return a * Math.exp(b * Math.pow(x, 2));
	}

	private RandomAccessibleInterval<T> dirac() {
		return createImage((x, y) -> (x == 0) && (y == 0) ? 1. : 0.);
	}

	private RandomAccessibleInterval<T> createImage(BiFunction<Long, Long, Double> content) {
		Img<T> image = createEmptyImage();
		RandomAccessibleInterval<Localizable> positions = Views.interval(Localizables.randomAccessible(
			image.numDimensions()), image);
		LoopBuilder.setImages(positions, image).forEachPixel((p, pixel) -> {
			long x = p.getLongPosition(0) - center;
			long y = p.getLongPosition(1) - center;
			pixel.setReal(content.apply(x, y));
		});
		return image;
	}

	private Img<T> createEmptyImage() {
		return new ArrayImgFactory<>(type).create(width, width);
	}

	private <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> deriveX(
		RandomAccessibleInterval<T> input)
	{
		Img<T> result = new ArrayImgFactory<>(Util.getTypeFromInterval(input)).create(Intervals
			.dimensionsAsLongArray(input));
		PartialDerivative.gradientCentralDifference(Views.extendBorder(input), result, 0);
		return result;
	}

	private RandomAccessibleInterval<T> secondDerivativeX(
		RandomAccessibleInterval<? extends RealType<?>> input)
	{
		Img<T> result = createEmptyImage();
		SeparableKernelConvolution.convolution1d(Kernel1D.centralAsymmetric(1, -2, 1), 0)
			.process(Views.extendBorder(input), result);
		return result;
	}
}
