package net.imglib2.algorithm.features;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import net.imglib2.*;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
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
			count += diff( stackA.getProcessor( slice ), stackB.getProcessor( slice ) );
		}
		return count;
	}

	public static < T extends Type< T >> boolean equals(final RandomAccessibleInterval< ? > a,
														final IterableInterval< T > b)
    {
    	if(!Intervals.equals(a, b))
    		return false;
        // create a cursor that automatically localizes itself on every move
		System.out.println("check picture content.");
        Cursor< T > bCursor = b.localizingCursor();
        RandomAccess< ? > aRandomAccess = a.randomAccess();
        while ( bCursor.hasNext())
        {
            bCursor.fwd();
            aRandomAccess.setPosition(bCursor);
            if( ! bCursor.get().equals( aRandomAccess.get() ))
            	return false;
        }
        return true;
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
		if(url == null)
			throw new NoSuchElementException("file: " + path);
		if ("file".equals(url.getProtocol())) return new ImagePlus(url.getPath());
		return new ImagePlus(url.toString());
	}

	public static void saveImageToResouce(final ImagePlus image, final String path) {
		final URL url = Utils.class.getResource(path);
		IJ.save(image, url.getPath());
	}

	public static <A extends IntegerType<A>, B extends IntegerType<B>>
		void assertImagesEqual(final IterableInterval<A> a, final RandomAccessibleInterval<B> b) {
		assertTrue(Intervals.equals(a, b));
		System.out.println("check picture content.");
		Cursor< A > aCursor = a.localizingCursor();
		RandomAccess< B > bRandomAccess = b.randomAccess();
		while ( aCursor.hasNext())
		{
			aCursor.fwd();
			bRandomAccess.setPosition(aCursor);
			assertEquals( bRandomAccess.get().getInteger(), aCursor.get().getInteger());
		}
	}

	public static <A extends Type<A>>
	void assertImagesEqual(final RandomAccessibleInterval<A> a, final RandomAccessibleInterval<A> b) {
		assertTrue(Intervals.equals(a, b));
		System.out.println("check picture content.");
		IntervalView<Pair<A, A>> pairs = Views.interval(Views.pair(a, b), b);
		Cursor<Pair<A, A>> cursor = pairs.cursor();
		while(cursor.hasNext()) {
			Pair<A,A> p = cursor.next();
			boolean equal = p.getA().valueEquals(p.getB());
			if(!equal)
				fail("Pixel values not equal on coordinate " +
						positionString(cursor) + ", expected: "
						+ p.getA() + " actual: " + p.getB());
		}
	}

	public static void assertImagesEqual(final ImagePlus expected, final RandomAccessibleInterval<FloatType> actual) {
		assertImagesEqual(ImagePlusAdapter.convertFloat(expected), actual);
	}

	public static void assertImagesEqual(final ImageProcessor expected, final RandomAccessibleInterval<FloatType> actual) {
		assertImagesEqual(new ImagePlus("expected", expected), actual);
	}

	private static <A extends Type<A>> String positionString(Cursor<Pair<A, A>> cursor) {
		StringJoiner joiner = new StringJoiner(", ");
		for (int i = 0, n = cursor.numDimensions(); i < n; i++)
			joiner.add(String.valueOf(cursor.getIntPosition(i)));
		return "(" + joiner + ")";
	}

	public static <T extends NumericType<T>> void showDifference(Img<T> expected, Img<T> actual) {
		showDifference((RandomAccessibleInterval<T>) expected, actual);
	}

	public static <T extends NumericType<T>> void showDifference(RandomAccessibleInterval<T> expectedImage, RandomAccessibleInterval<T> resultImage) {
		assertTrue(Intervals.equals(expectedImage, resultImage));
		showDifference(Views.iterable(expectedImage), Views.iterable(resultImage));
	}

	public static <T extends NumericType<T>> void showDifference(IterableInterval<T> expectedImage, IterableInterval<T> resultImage) {
		show(RevampUtils.ops().math().subtract(expectedImage, resultImage));
	}

	public static void show(Object... images) {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		for(Object o: images)
			imageJ.ui().show(translateToOrigin(o));
	}

	private static Object translateToOrigin(Object o) {
		if(o instanceof RandomAccessibleInterval)
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
		if(meanSquareError == 0.0)
			return Float.POSITIVE_INFINITY;
		return (20 * Math.log10(max(expected)) - 10 * Math.log10(meanSquareError));
	}

	private static <S extends ComplexType<S>, T extends ComplexType<T>> double meanSquareError(
			RandomAccessibleInterval<S> a, RandomAccessibleInterval<T> b)
	{
		if(!Intervals.equals(a, b))
			throw new IllegalArgumentException("both arguments must be the same interval" +
					"given: " + showInterval(a) + " and: " + showInterval(b));
		DoubleType sum = new DoubleType(0.0f);
		Views.interval(Views.pair(a, b), a).forEach(x -> sum.set(sum.get() + sqr(x.getA().getRealDouble() - x.getB().getRealDouble())));
		return sum.get() / Intervals.numElements(a);
	}

	private static String showInterval(Interval b) {
		StringJoiner j = new StringJoiner(", ");
		int n = b.numDimensions();
		for (int i = 0; i < n; i++) j.add(b.min(i) + " - " + b.max(i));
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

	public static void showPsnr(RandomAccessibleInterval<FloatType> expected, RandomAccessibleInterval<FloatType> actual) {
		System.out.println("psnr: " + psnr(expected, actual));
	}

	public static ImagePlus createImage(final String title, final int width, final int height, final int... pixels)
	{
		assertEquals( pixels.length, width * height );
		final byte[] bytes = new byte[pixels.length];
		for (int i = 0; i < bytes.length; i++) bytes[i] = (byte)pixels[i];
		final ByteProcessor bp = new ByteProcessor( width, height, bytes, null );
		return new ImagePlus( title, bp );
	}


	public static ImagePlus createImage(String title, int width, int height, final float... pixels) {
		assertEquals( pixels.length, width * height);
		final FloatProcessor processor = new FloatProcessor(width, height, pixels.clone());
		return new ImagePlus( title, processor);
	}

	public static <T> String pixelsAsString(RandomAccessibleInterval<T> image) {
		StringJoiner joiner = new StringJoiner(", ");
		for(T pixel : Views.iterable(image))
			joiner.add(pixel.toString());
		return "[" + joiner.toString() + "]";
	}

	public static <S extends ComplexType<S>, T extends ComplexType<T>> void assertImagesEqual(double expectedPsnr,
																							  RandomAccessibleInterval<S> expected, RandomAccessibleInterval<T> actual)
	{
		double psnr = Utils.psnr(expected, actual);
		if(RevampUtils.containsNaN(expected))
			fail("Cannot calculate PSNR because expected picture contains NaN value.");
		if(RevampUtils.containsNaN(actual))
			fail("Cannot calculate PSNR because actual picture contains NaN value.");
		if(psnr < expectedPsnr)
			fail("Actual PSNR is lower than expected. Actual: " + psnr + " Expected: " + expectedPsnr);
	}

	public static Img<FloatType> copy(Img<FloatType> input) {
		Img<FloatType> result = RevampUtils.ops().create().img(input);
		RevampUtils.ops().copy().iterableInterval(result, input);
		return result;
	}

	public static RandomAccessibleInterval<FloatType> subtract(RandomAccessibleInterval<FloatType> expected, RandomAccessibleInterval<FloatType> result) {
		RandomAccessibleInterval<Pair<FloatType, FloatType>> interval = Views.interval(Views.pair(expected, result), result);
		return Converters.convert(interval, (p, out) -> out.setReal(p.getA().get() - p.getB().get()), new FloatType());
	}

	public static RandomAccessibleInterval<IntType> toInt(RandomAccessibleInterval<FloatType> input) {
		Converter<FloatType, IntType> floatToInt = (in, out) -> out.set((int) in.get());
		return Converters.convert(input, floatToInt, new IntType());
	}

	public static ImageProcessor imageProcessor(RandomAccessibleInterval<FloatType> dy) {
		int width = (int) dy.dimension(0);
		int height = (int) dy.dimension(1);
		FloatProcessor processor = new FloatProcessor(width, height);
		RandomAccess<FloatType> ra = dy.randomAccess();
		for (int x=0; x<width; x++)
			for (int y=0; y<height; y++) {
				ra.setPosition(x, 0);
				ra.setPosition(y, 1);
				processor.setf(x, y, ra.get().get());
			}
		return processor;
	}

	static class StopWatch {

		private final long start = System.nanoTime();

		public Time get() {
			return Time.fromNanoSeconds(System.nanoTime() - start);
		}

		public String toString() {
			return get().toString();
		}
	}

	static class Time {
		private final double seconds;
		private Time(double seconds) {
			this.seconds = seconds;
		}
		static public Time fromNanoSeconds(double ns) {
			return fromSeconds(ns * 1e-9);
		}

		private static Time fromSeconds(double seconds) {
			return new Time(seconds);
		}

		public Time divide(int divider) {
			return fromSeconds(seconds / divider);
		}
		public Time add(Time time) {
			return fromSeconds(seconds + time.seconds);
		}
		public String toString() {
			if(seconds >= 1)
				return seconds + " s";
			if(seconds >= 1e-3)
				return seconds * 1e3 + " ms";
			if(seconds >= 1e-6)
				return seconds * 1e6 + " µs";
			return seconds * 1e9 + " ns";
		}
		public static Time zero() {
			return fromSeconds(0);
		}
	}

	static class TimeMeasurement {

		static private Map<String, Measurement> map = new HashMap<>();

		static void measure(String id, Runnable run) {
			StopWatch watch = new StopWatch();
			run.run();
			getMeasurement(id).add(watch.get());
		}

		private static Measurement getMeasurement(String id) {
			return map.computeIfAbsent(id, key -> new Measurement());
		}

		static class Measurement {
			int count = 0;
			Time time = Time.zero();

			public void add(Time time) {
				count++;
				this.time = this.time.add(time);
			}

			public Time mean() {
				return time.divide(count);
			}
		}

		public static void print() {
			map.forEach((key, measurement) -> System.out.print(key + ": " + measurement.mean() + "\n"));
		}

	}
}
