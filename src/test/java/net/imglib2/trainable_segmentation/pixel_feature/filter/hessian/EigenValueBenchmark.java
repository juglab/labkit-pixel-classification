
package net.imglib2.trainable_segmentation.pixel_feature.filter.hessian;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.algorithm.linalg.eigen.EigenValuesSymmetric;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmentation.RevampUtils;
import net.imglib2.trainable_segmentation.utils.CubicEquation;
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

import static net.imglib2.test.ImgLib2Assert.assertImageEquals;

@Fork(1)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class EigenValueBenchmark {

	private final long dims[] = { 50, 50, 50 };

	private final List<RandomAccessibleInterval<DoubleType>> slices = IntStream.range(0, 6)
		.mapToObj(i -> RandomImgs.seed(i).nextImage(new DoubleType(), dims))
		.collect(Collectors.toList());

	private final List<RandomAccessibleInterval<FloatType >> eigenvalues = IntStream.range(0, 3)
		.mapToObj(i -> ArrayImgs.floats(dims))
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
	public Object benchmarkImgLib2AlgorithmEigenValuesSymmetric3D() {
		RandomAccessibleInterval<Composite<DoubleType>> matrix = RevampUtils.vectorizeStack(slices);
		RandomAccessibleInterval<Composite<FloatType>> result = RevampUtils.vectorizeStack(
			eigenvalues);
		EigenValuesSymmetric<DoubleType, FloatType> calculator = EigenValues.symmetric(3);
		LoopBuilder.setImages(matrix, result).forEachPixel(
				(tensor, evs) -> calculator.compute(tensor, evs));
		return eigenvalues;
	}

	@Benchmark
	public Object benchmarkEigenValuesSymmetric3D() {
		RandomAccessibleInterval<Composite<DoubleType>> matrix = RevampUtils.vectorizeStack(slices);
		RandomAccessibleInterval<Composite<FloatType>> result = RevampUtils.vectorizeStack(
			eigenvalues);
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel(
				(matrix1, evs) -> calculator.compute(matrix1, evs));
		return eigenvalues;
	}

	@Benchmark
	public Object imglib2Composite() {
		RandomAccessibleInterval<Composite<DoubleType>> matrix =
				Cast.unchecked(Views.collapse(Views.stack(slices)));
		RandomAccessibleInterval<Composite<FloatType>> result =
				Cast.unchecked(Views.collapse(Views.stack(eigenvalues)));
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel( (matrix1, evs) -> calculator.compute(matrix1, evs));
		return eigenvalues;
	}

	@Benchmark
	public Object imglib2Composite2() {
		RandomAccessibleInterval<Composite<DoubleType>> matrix =
				Cast.unchecked(Views.collapse(blockInput));
		RandomAccessibleInterval<Composite<FloatType>> result =
				Cast.unchecked(Views.collapse(blockOutput));
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel( (matrix1, evs) -> calculator.compute(matrix1, evs));
		return eigenvalues;
	}

	@Benchmark
	public Object arrays() {
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
		EigenValueBenchmark one = new EigenValueBenchmark();
		one.arrays();
		EigenValueBenchmark two = new EigenValueBenchmark();
		two.benchmarkEigenValuesSymmetric3D();
		assertImageEquals(Views.stack(two.eigenvalues), Views.stack(one.eigenvalues));
	}
}
