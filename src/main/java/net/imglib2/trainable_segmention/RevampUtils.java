
package net.imglib2.trainable_segmention;

import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.StackView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import weka.core.DenseInstance;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class RevampUtils {

	private static final float[] SOBEL_FILTER_X_VALUES = { 1f, 2f, 1f, 0f, 0f, 0f, -1f, -2f, -1f };
	private static final RandomAccessibleInterval<FloatType> SOBEL_FILTER_X = ArrayImgs.floats(
		SOBEL_FILTER_X_VALUES, 3, 3);
	private static final float[] SOBEL_FILTER_Y_VALUES = { 1f, 0f, -1f, 2f, 0f, -2f, 1f, 0f, -1f };
	private static final RandomAccessibleInterval<FloatType> SOBEL_FILTER_Y = ArrayImgs.floats(
		SOBEL_FILTER_Y_VALUES, 3, 3);

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

	// TODO: move to Intervals?
	public static Interval appendDimensionToInterval(Interval in, long min, long max) {
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

	public static RandomAccessibleInterval<FloatType> gauss(OpService ops,
		RandomAccessibleInterval<FloatType> image, double[] sigmas)
	{
		return gauss(ops, Views.extendBorder(image), (Interval) image, sigmas);
	}

	public static RandomAccessibleInterval<FloatType> gauss(OpEnvironment ops,
		RandomAccessible<FloatType> input, Interval outputInterval, double[] sigmas)
	{
		RandomAccessibleInterval<FloatType> blurred = ops.create().img(outputInterval, new FloatType());
		Gauss3.gauss(sigmas, input, blurred);
		return blurred;
	}

	public static Interval gaussRequiredInput(Interval outputInterval, double[] sigmas) {
		long[] border = IntStream.of(Gauss3.halfkernelsizes(sigmas))
			.mapToLong(x -> x - 1)
			.toArray();
		return Intervals.expand(outputInterval, border);
	}

	public static RandomAccessibleInterval<FloatType> deriveX(OpEnvironment ops,
		RandomAccessible<FloatType> input, Interval outputInterval)
	{
		if (outputInterval.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		RandomAccessibleInterval<FloatType> output = ops.create().img(outputInterval, new FloatType());
		ops.filter().convolve(output, input, SOBEL_FILTER_X);
		return output;
	}

	public static Interval deriveXRequiredInput(Interval output) {
		if (output.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		return Intervals.expand(output, new long[] { 1, 1 });
	}

	public static RandomAccessibleInterval<FloatType> deriveY(OpEnvironment ops,
		RandomAccessible<FloatType> input, Interval outputInterval)
	{
		if (outputInterval.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		RandomAccessibleInterval<FloatType> output = ops.create().img(outputInterval, new FloatType());
		ops.filter().convolve(output, input, SOBEL_FILTER_Y);
		return output;
	}

	public static Interval deriveYRequiredInput(Interval output) {
		if (output.numDimensions() != 2)
			throw new IllegalArgumentException("Only two dimensional images supported.");
		return Intervals.expand(output, new long[] { 1, 1 });
	}

	public static RandomAccessibleInterval<FloatType> convolve(OpService ops,
		RandomAccessibleInterval<FloatType> blurred, RandomAccessibleInterval<FloatType> kernel)
	{
		return ops.filter().convolve(blurred, kernel, new OutOfBoundsBorderFactory<>());
	}

	public static RandomAccessible<FloatType> randomAccessibleToFloat(
		RandomAccessible<? extends RealType<?>> input)
	{
		RealType<?> inputType = input.randomAccess().get();
		if (inputType instanceof FloatType)
			return uncheckedCast(input);
		Converter<RealType<?>, FloatType> converter = RealTypeConverters.getConverter(inputType,
			new FloatType());
		return Converters.convert(input, converter, new FloatType());
	}

	static private Img<FloatType> copy(OpEnvironment ops, IterableInterval<FloatType> input) {
		Img<FloatType> result = ops.create().img(input, input.firstElement());
		ops.copy().iterableInterval(result, input);
		return result;
	}

	public static Img<FloatType> copy(OpEnvironment ops, RandomAccessibleInterval<FloatType> input) {
		return copy(ops, Views.iterable(input));
	}

	public static boolean containsNaN(RandomAccessibleInterval<? extends ComplexType<?>> result) {
		for (ComplexType<?> value : Views.iterable(result))
			if (Double.isNaN(value.getRealDouble()))
				return true;
		return false;
	}

	public static long[] nCopies(int count, long value) {
		long[] result = new long[count];
		Arrays.fill(result, value);
		return result;
	}

	public static void wrapException(RunnableWithException r) {
		try {
			r.run();
		}
		catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	public static <R> R wrapException(SupplierWithException<R> r) {
		try {
			return r.get();
		}
		catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	public static DenseInstance getInstance(int featureCount, int classIndex,
		Composite<? extends RealType<?>> featureValues)
	{
		double[] values = new double[featureCount + 1];
		for (int i = 0; i < featureCount; i++)
			values[i] = featureValues.get(i).getRealDouble();
		values[featureCount] = classIndex;
		return new DenseInstance(1.0, values);
	}

	public static List<RandomAccessible<FloatType>> splitChannels(RandomAccessible<ARGBType> image) {
		return convertersStream().map(x -> Converters.convert(image, x, new FloatType())).collect(
			Collectors.toList());
	}

	public static List<RandomAccessibleInterval<FloatType>> splitChannels(
		RandomAccessibleInterval<ARGBType> image)
	{
		return convertersStream().map(x -> Converters.convert(image, x, new FloatType())).collect(
			Collectors.toList());
	}

	private static Stream<Converter<ARGBType, FloatType>> convertersStream() {
		return Stream.<IntToIntFunction> of(ARGBType::red, ARGBType::green, ARGBType::blue).map(
			RevampUtils::converter);
	}

	private static Converter<ARGBType, FloatType> converter(IntToIntFunction f) {
		return (in, out) -> out.set(((float) f.apply(in.get())) / 255.f);
	}

	public static <T> T uncheckedCast(Object input) {
		@SuppressWarnings("unchecked")
		T result = (T) input;
		return result;
	}

	public static <T> RandomAccessibleInterval<Composite<T>> vectorizeStack(
		RandomAccessibleInterval<T>... derivatives)
	{
		return vectorizeStack(Arrays.asList(derivatives));
	}

	public static <T> RandomAccessibleInterval<Composite<T>> vectorizeStack(
		List<RandomAccessibleInterval<T>> derivatives)
	{
		return Cast.unchecked(Views.collapse(Views.stack(
			StackView.StackAccessMode.MOVE_ALL_SLICE_ACCESSES, derivatives)));
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

	public static Interval intervalRemoveDimension(Interval interval) {
		return intervalRemoveDimension(interval, interval.numDimensions() - 1);
	}

	public static Interval intervalRemoveDimension(Interval interval,
		int d)
	{
		long[] min = removeElement(Intervals.minAsLongArray(interval), d);
		long[] max = removeElement(Intervals.maxAsLongArray(interval), d);
		return new FinalInterval(min, max);
	}

	private static long[] removeElement(long[] values, int d) {
		long[] result = new long[values.length - 1];
		System.arraycopy(values, 0, result, 0, d);
		System.arraycopy(values, d + 1, result, d, values.length - d - 1);
		return result;
	}
}
