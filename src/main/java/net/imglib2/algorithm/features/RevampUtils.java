package net.imglib2.algorithm.features;

import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.Context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Matthias Arzt
 */
public class RevampUtils {

	private static final OpService ops = new Context(OpService.class).service(OpService.class);
	private static final float[] SOBEL_FILTER_X_VALUES = {1f,2f,1f,0f,0f,0f,-1f,-2f,-1f};
	private static final RandomAccessibleInterval<FloatType> SOBEL_FILTER_X = ArrayImgs.floats(SOBEL_FILTER_X_VALUES, 3, 3);
	private static final float[] SOBEL_FILTER_Y_VALUES = {1f,0f,-1f,2f,0f,-2f,1f,0f,-1f};
	private static final RandomAccessibleInterval<FloatType> SOBEL_FILTER_Y = ArrayImgs.floats(SOBEL_FILTER_Y_VALUES, 3, 3);

	public static OpService ops() {
		return ops;
	}

	public static List<RandomAccessibleInterval<FloatType>> slices(RandomAccessibleInterval<FloatType> output) {
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

	public static RandomAccessibleInterval<FloatType> deriveX(RandomAccessibleInterval<FloatType> in) {
		return convolve(in, SOBEL_FILTER_X);
	}

	public static RandomAccessibleInterval<FloatType> deriveY(RandomAccessibleInterval<FloatType> in) {
		return convolve(in, SOBEL_FILTER_Y);
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

	public static void copy(RandomAccessibleInterval<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		ops().copy().iterableInterval(Views.iterable(out), Views.iterable(in));
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
}
