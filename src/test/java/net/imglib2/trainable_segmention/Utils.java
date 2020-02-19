
package net.imglib2.trainable_segmention;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.script.ScriptService;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Matthias Arzt
 */
public class Utils {

	private static final OpEnvironment ops = new Context(OpService.class, ScriptService.class)
		.service(OpService.class);

	public static OpEnvironment ops() {
		return ops;
	}

	public static void assertImagesEqual(ImagePlus expected, ImagePlus actual) {
		assertTrue(diffImagePlus(expected, actual) == 0);
	}

	private static int diffImagePlus(final ImagePlus a, final ImagePlus b) {
		final int[] dimsA = a.getDimensions(), dimsB = b.getDimensions();
		if (dimsA.length != dimsB.length) return dimsA.length - dimsB.length;
		for (int i = 0; i < dimsA.length; i++) {
			if (dimsA[i] != dimsB[i]) return dimsA[i] - dimsB[i];
		}
		int count = 0;
		final ImageStack stackA = a.getStack(), stackB = b.getStack();
		for (int slice = 1; slice <= stackA.getSize(); slice++) {
			count += diff(stackA.getProcessor(slice), stackB.getProcessor(slice));
		}
		return count;
	}

	private static int diff(final ImageProcessor a, final ImageProcessor b) {
		int count = 0;
		final int width = a.getWidth(), height = a.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (a.getf(x, y) != b.getf(x, y)) count++;
			}
		}
		return count;
	}

	public static ImagePlus loadImagePlusFromResource(final String path) {
		final URL url = Utils.class.getResource("/" + path);
		if (url == null)
			throw new NoSuchElementException("file: " + path);
		if ("file".equals(url.getProtocol())) return new ImagePlus(url.getPath());
		return new ImagePlus(url.toString());
	}

	public static void saveImageToResouce(final ImagePlus image, final String path) {
		final URL url = Utils.class.getResource(path);
		IJ.save(image, url.getPath());
	}

	public static <A extends Type<A>>
		void assertImagesEqual(final RandomAccessibleInterval<? extends A> a,
			final RandomAccessibleInterval<? extends A> b)
	{
		ImgLib2Assert.assertImageEquals(a, b);
	}

	public static <A extends Type<A>> void assertIntervalEquals(
		Interval expected,
		Interval actual)
	{
		if (!Intervals.equals(expected, actual))
			fail("Intervals differ, expected = " + showInterval(expected) + ", actual = " + showInterval(
				actual));
	}

	public static void assertImagesEqual(final ImagePlus expected,
		final RandomAccessibleInterval<FloatType> actual)
	{
		assertImagesEqual(ImagePlusAdapter.convertFloat(expected), actual);
	}

	public static void assertImagesEqual(final ImageProcessor expected,
		final RandomAccessibleInterval<FloatType> actual)
	{
		assertImagesEqual(new ImagePlus("expected", expected), actual);
	}

	private static <A extends Type<A>> String positionString(Localizable cursor) {
		StringJoiner joiner = new StringJoiner(", ");
		for (int i = 0, n = cursor.numDimensions(); i < n; i++)
			joiner.add(String.valueOf(cursor.getIntPosition(i)));
		return "(" + joiner + ")";
	}

	public static <T extends NumericType<T>> void showDifference(Img<T> expected, Img<T> actual) {
		showDifference((RandomAccessibleInterval<T>) expected, actual);
	}

	public static <T extends NumericType<T>> void showDifference(
		RandomAccessibleInterval<T> expectedImage, RandomAccessibleInterval<T> resultImage)
	{
		assertTrue(Intervals.equals(expectedImage, resultImage));
		showDifference(Views.iterable(expectedImage), Views.iterable(resultImage));
	}

	public static <T extends NumericType<T>> void showDifference(IterableInterval<T> expectedImage,
		IterableInterval<T> resultImage)
	{
		show(ops().math().subtract(expectedImage, resultImage));
	}

	public static void show(Object... images) {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		for (Object o : images)
			imageJ.ui().show(translateToOrigin(o));
	}

	private static Object translateToOrigin(Object o) {
		if (o instanceof RandomAccessibleInterval)
			return Views.zeroMin((RandomAccessibleInterval<?>) o);
		return o;
	}

	public static RandomAccessibleInterval<IntType> loadImageIntType(String s) {
		RandomAccessibleInterval<UnsignedByteType> img = ImagePlusAdapter.wrapByte(loadImage(s));
		return Converters.convert(img, (b, i) -> i.set(b.get()), new IntType());
	}

	public static RandomAccessibleInterval<FloatType> loadImageFloatType(String s) {
		return ImagePlusAdapter.wrapFloat(loadImage(s));
	}

	public static ImagePlus loadImage(String s) {
		return Utils.loadImagePlusFromResource(s);
	}

	public static <S extends ComplexType<S>, T extends ComplexType<T>> double psnr(
		RandomAccessibleInterval<S> expected, RandomAccessibleInterval<T> actual)
	{
		double meanSquareError = meanSquareError(expected, actual);
		if (meanSquareError == 0.0)
			return Float.POSITIVE_INFINITY;
		return (20 * Math.log10(max(expected)) - 10 * Math.log10(meanSquareError));
	}

	private static <S extends ComplexType<S>, T extends ComplexType<T>> double meanSquareError(
		RandomAccessibleInterval<S> a, RandomAccessibleInterval<T> b)
	{
		if (!Intervals.equals(a, b))
			throw new IllegalArgumentException("both arguments must be the same interval" +
				"given: " + showInterval(a) + " and: " + showInterval(b));
		DoubleType sum = new DoubleType(0.0f);
		Views.interval(Views.pair(a, b), a).forEach(x -> sum.set(sum.get() + sqr(x.getA()
			.getRealDouble() - x.getB().getRealDouble())));
		return sum.get() / Intervals.numElements(a);
	}

	private static String showInterval(Interval b) {
		StringJoiner j = new StringJoiner(", ");
		int n = b.numDimensions();
		for (int i = 0; i < n; i++)
			j.add(b.min(i) + " - " + b.max(i));
		return "[" + j + "]";
	}

	private static double sqr(double v) {
		return v * v;
	}

	private static <T extends ComplexType<T>> double max(RandomAccessibleInterval<T> a) {
		IntervalView<T> interval = Views.interval(a, a);
		T result = interval.firstElement().createVariable();
		interval.forEach(x -> result.setReal(Math.max(result.getRealDouble(), x.getRealDouble())));
		return result.getRealDouble();
	}

	public static void showPsnr(RandomAccessibleInterval<FloatType> expected,
		RandomAccessibleInterval<FloatType> actual)
	{
		System.out.println("psnr: " + psnr(expected, actual));
	}

	public static ImagePlus createImage(final String title, final int width, final int height,
		final int... pixels)
	{
		assertEquals(pixels.length, width * height);
		final byte[] bytes = new byte[pixels.length];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) pixels[i];
		final ByteProcessor bp = new ByteProcessor(width, height, bytes, null);
		return new ImagePlus(title, bp);
	}

	public static ImagePlus createImage(String title, int width, int height, final float... pixels) {
		assertEquals(pixels.length, width * height);
		final FloatProcessor processor = new FloatProcessor(width, height, pixels.clone());
		return new ImagePlus(title, processor);
	}

	public static <T> String pixelsAsString(RandomAccessibleInterval<T> image) {
		StringJoiner joiner = new StringJoiner(", ");
		for (T pixel : Views.iterable(image))
			joiner.add(pixel.toString());
		return "[" + joiner.toString() + "]";
	}

	public static <S extends ComplexType<S>, T extends ComplexType<T>> void assertImagesEqual(
		double expectedPsnr,
		RandomAccessibleInterval<S> expected, RandomAccessibleInterval<T> actual)
	{
		double psnr = Utils.psnr(expected, actual);
		if (RevampUtils.containsNaN(expected))
			fail("Cannot calculate PSNR because expected picture contains NaN value.");
		if (RevampUtils.containsNaN(actual))
			fail("Cannot calculate PSNR because actual picture contains NaN value.");
		if (psnr < expectedPsnr)
			fail("Actual PSNR is lower than expected. Actual: " + psnr + " Expected: " + expectedPsnr);
	}

	public static Img<FloatType> copy(Img<FloatType> input) {
		Img<FloatType> result = ops().create().img(input);
		ops().copy().iterableInterval(result, input);
		return result;
	}

	public static RandomAccessibleInterval<FloatType> subtract(
		RandomAccessibleInterval<FloatType> expected, RandomAccessibleInterval<FloatType> result)
	{
		RandomAccessibleInterval<Pair<FloatType, FloatType>> interval = Views.interval(Views.pair(
			expected, result), result);
		return Converters.convert(interval, (p, out) -> out.setReal(p.getA().get() - p.getB().get()),
			new FloatType());
	}

	public static RandomAccessibleInterval<IntType> toInt(RandomAccessibleInterval<FloatType> input) {
		Converter<FloatType, IntType> floatToInt = (in, out) -> out.set((int) in.get());
		return Converters.convert(input, floatToInt, new IntType());
	}

}
