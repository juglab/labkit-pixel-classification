package net.imglib2.algorithm.features;

import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.Context;
import org.scijava.script.ScriptService;
import weka.core.DenseInstance;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class RevampUtils {

	private static final OpService ops = new Context(OpService.class, ScriptService.class).service(OpService.class);
	private static final float[] SOBEL_FILTER_X_VALUES = {1f,2f,1f,0f,0f,0f,-1f,-2f,-1f};
	private static final RandomAccessibleInterval<FloatType> SOBEL_FILTER_X = ArrayImgs.floats(SOBEL_FILTER_X_VALUES, 3, 3);
	private static final float[] SOBEL_FILTER_Y_VALUES = {1f,0f,-1f,2f,0f,-2f,1f,0f,-1f};
	private static final RandomAccessibleInterval<FloatType> SOBEL_FILTER_Y = ArrayImgs.floats(SOBEL_FILTER_Y_VALUES, 3, 3);

	public static OpService ops() {
		return ops;
	}

	public static <T> List<RandomAccessibleInterval<T>> slices(RandomAccessibleInterval<T> output) {
		int axis = output.numDimensions() - 1;
		return LongStream.range(output.min(axis), output.max(axis) + 1)
				.mapToObj(pos -> Views.hyperSlice(output, axis, pos))
				.collect(Collectors.toList());
	}

	public static long[] extend(long[] in, long elem) {
		long result[] = new long[in.length + 1];
		System.arraycopy(in, 0, result, 0, in.length);
		result[in.length] = elem;
		return result;
	}

	public static int[] extend(int[] in, int elem) {
		int result[] = new int[in.length + 1];
		System.arraycopy(in, 0, result, 0, in.length);
		result[in.length] = elem;
		return result;
	}

	public static Interval extend(Interval in, long min, long max) {
		int n = in.numDimensions();
		long[] mins = new long[n + 1];
		long[] maxs = new long[n + 1];
		for (int i = 0; i < n; i++) {
			mins[i] = in.min(i);
			maxs[i] = in.max(i);
		}
		mins[n] = min;
		maxs[n] = max;
		return new FinalInterval(mins, maxs);
	}

	public static Interval removeLastDimension(Interval in) {
		long[] min = removeLast(Intervals.minAsLongArray(in));
		long[] max = removeLast(Intervals.maxAsLongArray(in));
		return new FinalInterval(min, max);
	}

	private static long[] removeLast(long[] longs) {
		return Arrays.copyOf(longs, longs.length - 1);
	}

	public static RandomAccessibleInterval<FloatType> gauss(RandomAccessibleInterval<FloatType> image, double[] sigmas) {
		RandomAccessibleInterval<FloatType> blurred = ops().create().img(image);
		ops().filter().gauss(blurred, image, sigmas, new OutOfBoundsBorderFactory<>());
		return blurred;
	}

	public static RandomAccessibleInterval<FloatType> gauss(RandomAccessible<FloatType> input, Interval outputInterval, double[] sigmas) {
		RandomAccessibleInterval<FloatType> blurred = RevampUtils.ops().create().img(outputInterval, new FloatType());
		try {
			Gauss3.gauss(sigmas, input, blurred, Executors.newSingleThreadExecutor());
		} catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
		return blurred;
	}

	public static Interval gaussRequiredInput(Interval outputInterval, double[] sigmas) {
		long[] border = IntStream.of(Gauss3.halfkernelsizes(sigmas))
				.mapToLong(x -> x - 1)
				.toArray();
		return Intervals.expand(outputInterval, border);
	}

	public static RandomAccessibleInterval<FloatType> deriveX(RandomAccessible<FloatType> input, Interval outputInterval) {
		if(outputInterval.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		RandomAccessibleInterval<FloatType> output = RevampUtils.ops().create().img(outputInterval, new FloatType());
		ops().filter().convolve(output, input, SOBEL_FILTER_X);
		return output;
	}

	public static Interval deriveXRequiredInput(Interval output) {
		if(output.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		return Intervals.expand(output, new long[]{1,1});
	}

	public static RandomAccessibleInterval<FloatType> deriveY(RandomAccessible<FloatType> input, Interval outputInterval) {
		if(outputInterval.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		RandomAccessibleInterval<FloatType> output = RevampUtils.ops().create().img(outputInterval, new FloatType());
		ops().filter().convolve(output, input, SOBEL_FILTER_Y);
		return output;
	}

	public static Interval deriveYRequiredInput(Interval output) {
		if(output.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		return Intervals.expand(output, new long[]{1,1});
	}

	public static RandomAccessibleInterval<FloatType> convolve(RandomAccessibleInterval<FloatType> blurred, RandomAccessibleInterval<FloatType> kernel) {
		return ops().filter().convolve(blurred, kernel, new OutOfBoundsBorderFactory<>());
	}

	public static RandomAccessibleInterval<IntType> toInt(RandomAccessibleInterval<FloatType> input) {
		Converter<FloatType, IntType> floatToInt = (in, out) -> out.set((int) in.get());
		return Converters.convert(input, floatToInt, new IntType());
	}

	public static RandomAccessibleInterval<FloatType> toFloat(RandomAccessibleInterval<IntType> input) {
		Converter<IntType, FloatType> intToFloat = (in, out) -> out.set(in.get());
		return Converters.convert(input, intToFloat, new FloatType());
	}

	static private Img<FloatType> copy(IterableInterval<FloatType> input) {
		Img<FloatType> result = ops().create().img(input, input.firstElement());
		ops().copy().iterableInterval(result, input);
		return result;
	}

	public static Img<FloatType> copy(RandomAccessibleInterval<FloatType> input) {
		return copy(Views.iterable(input));
	}

	public static <T extends ComplexType<T>> boolean containsNaN(RandomAccessibleInterval<T> result) {
		for(T value : Views.iterable(result))
			if(Double.isNaN(value.getRealDouble()))
				return true;
		return false;
	}

	public static long[] nCopies(int count, long value) {
		long[] result = new long[count];
		Arrays.fill(result, value);
		return result;
	}

	public static double[] nCopies(int count, double value) {
		double[] result = new double[count];
		Arrays.fill(result, value);
		return result;
	}

	public static Iterable<Localizable> neigborsLocations(int n) {
		Img<ByteType> img = ArrayImgs.bytes(nCopies(n, 3));
		IntervalView<ByteType> translate = Views.translate(img, nCopies(n, -1));
		Cursor<ByteType> cursor = translate.localizingCursor();

		return () -> new java.util.Iterator<Localizable>() {
			@Override
			public boolean hasNext() {
				return cursor.hasNext();
			}

			@Override
			public Localizable next() {
				cursor.fwd();
				return cursor;
			}
		};
	}

	public static double distance(Localizable a, Localizable b) {
		int n = a.numDimensions();
		long sum = 0;
		for (int i = 0; i < n; i++) {
			long difference = a.getLongPosition(i) - b.getLongPosition(i);
			sum += difference * difference;
		}
		return Math.sqrt(sum);
	}

	public static <T> List<T> filterForClass(Class<T> tClass, List<?> in) {
		return in.stream().filter(tClass::isInstance).map(tClass::cast).collect(Collectors.toList());
	}

	public static Object[] prepend(Object x, Object[] xs) {
		return Stream.concat(Stream.of(x), Stream.of(xs)).toArray();
	}

	public static void wrapException(RunnableWithException r) {
		try {
			r.run();
		} catch(Exception e) {
			if(e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	public static <R> R wrapException(SupplierWithException<R> r) {
		try {
			return r.get();
		} catch(Exception e) {
			if(e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	public static DenseInstance getInstance(int featureCount, int classIndex, Composite<? extends RealType<?>> featureValues) {
		double[] values = new double[featureCount + 1];
		for (int i = 0; i < featureCount; i++)
			values[i] = featureValues.get(i).getRealDouble();
		values[featureCount] = classIndex;
		return new DenseInstance(1.0, values);
	}

	public static void copyInteger(RandomAccessibleInterval<? extends IntegerType<?>> in, RandomAccessibleInterval<? extends IntegerType<?>> result) {
		Views.interval(Views.pair(in, result), in).forEach(p -> p.getB().setInteger(p.getA().getInteger()));
	}

	public static List<RandomAccessible<FloatType>> splitChannels(RandomAccessible<ARGBType> image) {
		return convertersStream().map(x -> Converters.convert(image, x, new FloatType())).collect(Collectors.toList());
	}

	public static List<RandomAccessibleInterval<FloatType>> splitChannels(RandomAccessibleInterval<ARGBType> image) {
		return convertersStream().map(x -> Converters.convert(image, x, new FloatType())).collect(Collectors.toList());
	}

	private static Stream<Converter<ARGBType, FloatType>> convertersStream() {
		return Stream.<IntToIntFunction>of(ARGBType::red, ARGBType::green, ARGBType::blue) .map(RevampUtils::converter);
	}

	private static Converter<ARGBType, FloatType> converter(IntToIntFunction f) {
		return (in, out) -> out.set(((float) f.apply(in.get())) / 255.f);
	}

	public static <T> T uncheckedCast(Object input) {
		@SuppressWarnings("unchecked") T result = (T) input;
		return result;
	}

	static <T> RandomAccessible<T> castRandomAccessible(RandomAccessible<?> input, Class<T> tClass) {
		if(tClass.isInstance(input.randomAccess().get()))
			return uncheckedCast(input);
		throw new IllegalArgumentException("RandomAccessible input must be of type " + tClass.getName());
	}

	public interface RunnableWithException {
		void run() throws Exception;
	}

	public interface SupplierWithException<R> {
		R get() throws Exception;
	}

	private interface IntToIntFunction {
		int apply(int value);
	}
}
