
package net.imglib2.trainable_segmentation.pixel_feature.filter.hessian;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmentation.RevampUtils;
import net.imglib2.trainable_segmentation.utils.CubicEquation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import preview.net.imglib2.converter.RealTypeConverters;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * WIP: This benchmark compares alternatives to using a combination of
 * LoopBuilder + Views.collapse(Views.stack(...))
 * <p>
 * Conclusion: LoopBuilder + collapse + stack. Is very slow. Performance is half
 * as good as using arrays directly. Views.stack(...) seems to add the most overhead.
 * We get good performance by using cursors directly on ArrayImgs.
 * Medium performance can be achieved by avoiding the use of Views.stack(), but
 * this requires the input to by a multichannel image instead of a list of images.
 * VectorRandomAccess achieves better performance than the combination Views.stack +
 * Views.collapse but is still not great.
 */
@Fork(1)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class EigenValueBenchmark {

	private final long dims[] = { 50, 50, 50 };

	private final List<RandomAccessibleInterval<DoubleType>> slices = IntStream.range(0, 6)
		.mapToObj(i -> RandomImgs.seed(i).randomize(image(new DoubleType())))
		.collect(Collectors.toList());

	private <T extends NativeType<T>> RandomAccessibleInterval<T> image(T type) {
		return new ArrayImgFactory<>(type).create(dims);
	}

	private final List<RandomAccessibleInterval<FloatType >> eigenvalues = IntStream.range(0, 3)
		.mapToObj(i -> image(new FloatType()))
		.collect(Collectors.toList());

	private final RandomAccessibleInterval<DoubleType> blockInput = init();

	private RandomAccessibleInterval<DoubleType> init() {
		RandomAccessibleInterval< DoubleType > doubles =
				ArrayImgs.doubles(50, 50, 50, 6);
		RealTypeConverters.copyFromTo(Views.stack(slices), doubles);
		return doubles;
	}

	private final RandomAccessibleInterval<FloatType>
			blockOutput = ArrayImgs.floats(50, 50, 50, 3);

	@Benchmark
	public Object useVectorRandomAccess() {
		// NB: This is a little bit faster than Views.collapse + Views.stack but still has the same syntax.
		RandomAccessibleInterval<Composite<DoubleType>> matrix = RevampUtils.vectorizeStack(slices);
		RandomAccessibleInterval<Composite<FloatType>> result = RevampUtils.vectorizeStack(
			eigenvalues);
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel(
				(matrix1, evs) -> calculator.compute(matrix1, evs));
		return eigenvalues;
	}

	@Benchmark
	public Object useImgLib2ViewsCollapseAndStack() {
		// NB: this is slowest
		RandomAccessibleInterval<Composite<DoubleType>> matrix =
				Cast.unchecked(Views.collapse(Views.stack(slices)));
		RandomAccessibleInterval<Composite<FloatType>> result =
				Cast.unchecked(Views.collapse(Views.stack(eigenvalues)));
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel( (matrix1, evs) -> calculator.compute(matrix1, evs));
		return eigenvalues;
	}

	@Benchmark
	public Object useNoLoopBuilder() {
		// NB: This shows that using LoopBuilder is not the problem.
		RandomAccessibleInterval<Composite<DoubleType>> matrix =
				Cast.unchecked(Views.collapse(Views.stack(slices)));
		RandomAccessibleInterval<Composite<FloatType>> result =
				Cast.unchecked(Views.collapse(Views.stack(eigenvalues)));
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		Cursor< Composite< DoubleType > > m = Views.flatIterable(matrix).cursor();
		Cursor< Composite< FloatType > > e = Views.flatIterable(result).cursor();
		while (m.hasNext()) {
			calculator.compute(m.next(), e.next());
		}
		return eigenvalues;
	}

	@Benchmark
	public Object useImgLib2ViewsCollapseOnly() {
		RandomAccessibleInterval<Composite<DoubleType>> matrix = Cast.unchecked(Views.collapse(blockInput));
		RandomAccessibleInterval<Composite<FloatType>> result = Cast.unchecked(Views.collapse(blockOutput));
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel( (matrix1, evs) -> calculator.compute(matrix1, evs));
		return eigenvalues;
	}

	@Benchmark
	public Object useArrays() {
		double[] i0 = getArray(slices.get(0));
		double[] i1 = getArray(slices.get(1));
		double[] i2 = getArray(slices.get(2));
		double[] i3 = getArray(slices.get(3));
		double[] i4 = getArray(slices.get(4));
		double[] i5 = getArray(slices.get(5));
		float[] o0 = getArray(eigenvalues.get(0));
		float[] o1 = getArray(eigenvalues.get(1));
		float[] o2 = getArray(eigenvalues.get(2));
		double[] e = new double[3];
		for (int z = 0; z < 50; z++) {
			for (int y = 0; y < 50; y++) {
				for (int x = 0; x < 50; x++) {
					int o = (z * 50 + y) * 50 + x;
					final double a11 = i0[o];
					final double a12 = i1[o];
					final double a13 = i2[o];
					final double a22 = i3[o];
					final double a23 = i4[o];
					final double a33 = i5[o];
					calc(e, a11, a12, a13, a22, a23, a33);
					o0[o] = (float) e[2];
					o1[o] = (float) e[1];
					o2[o] = (float) e[0];
				}
			}
		}
		return eigenvalues;
	}

	@Benchmark
	public Object useCursors() {
		Cursor< DoubleType > i0 = Views.flatIterable(slices.get(0)).cursor();
		Cursor< DoubleType > i1 = Views.flatIterable(slices.get(1)).cursor();
		Cursor< DoubleType > i2 = Views.flatIterable(slices.get(2)).cursor();
		Cursor< DoubleType > i3 = Views.flatIterable(slices.get(3)).cursor();
		Cursor< DoubleType > i4 = Views.flatIterable(slices.get(4)).cursor();
		Cursor< DoubleType > i5 = Views.flatIterable(slices.get(5)).cursor();
		Cursor<FloatType> o0 = Views.flatIterable(eigenvalues.get(0)).cursor();
		Cursor<FloatType> o1 = Views.flatIterable(eigenvalues.get(1)).cursor();
		Cursor<FloatType> o2 = Views.flatIterable(eigenvalues.get(2)).cursor();
		double[] e = new double[3];
		while (o1.hasNext()) {
			final double a11 = i0.next().getRealDouble();
			final double a12 = i1.next().getRealDouble();
			final double a13 = i2.next().getRealDouble();
			final double a22 = i3.next().getRealDouble();
			final double a23 = i4.next().getRealDouble();
			final double a33 = i5.next().getRealDouble();
			calc(e, a11, a12, a13, a22, a23, a33);
			o0.next().setReal(e[2]);
			o1.next().setReal(e[1]);
			o2.next().setReal(e[0]);
		}
		return eigenvalues;
	}

	public <T> T getArray(RandomAccessibleInterval arrayImg) {
		return Cast.unchecked(((ArrayDataAccess) ((ArrayImg) arrayImg).update(null)).getCurrentStorageArray());
	}

	public void calc(double[] x, double a11, double a12, double a13, double a22,
			double a23, double a33)
	{
		final double b2 = -(a11 + a22 + a33);
		final double b1 = a11 * a22 + a11 * a33 + a22 * a33 - a12 * a12 - a13 * a13 - a23 * a23;
		final double b0 = a11 * (a23 * a23 - a22 * a33) + a22 * a13 * a13 + a33 * a12 * a12 - 2 * a12 *
				a13 * a23;
		CubicEquation.solveNormalized(b0, b1, b2, x);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(EigenValueBenchmark.class.getSimpleName())
			.build();
		new Runner(options).run();
	}
}
