
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.algorithm.linalg.eigen.EigenValuesSymmetric;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.StackView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;
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
import preview.net.imglib2.loops.LoopBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Fork(1)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class EigenValueBenchmark {

	private final long dims[] = { 50, 50, 50 };

	private final List<RandomAccessibleInterval<DoubleType>> slices = IntStream.range(0, 6)
		.mapToObj(i -> RandomImgs.seed(i).nextImage(new DoubleType(), dims))
		.collect(Collectors.toList());

	private final List<RandomAccessibleInterval<DoubleType>> eigenvalues = IntStream.range(0, 6)
		.mapToObj(i -> ArrayImgs.doubles(dims))
		.collect(Collectors.toList());

	@Benchmark
	public Object benchmarkImgLib2AlgorithmEigenValuesSymmetric3D() {
		RandomAccessibleInterval<? extends GenericComposite<DoubleType>> matrix = collapse(slices);
		RandomAccessibleInterval<? extends GenericComposite<DoubleType>> result = collapse(eigenvalues);
		EigenValuesSymmetric<DoubleType, DoubleType> calculator = EigenValues.symmetric(3);
		LoopBuilder.setImages(matrix, result).forEachPixel(calculator::compute);
		return eigenvalues;
	}

	@Benchmark
	public Object benchmarkEigenValuesSymmetric3D() {
		RandomAccessibleInterval<? extends GenericComposite<DoubleType>> matrix = collapse(slices);
		RandomAccessibleInterval<? extends GenericComposite<DoubleType>> result = collapse(eigenvalues);
		EigenValues<DoubleType, DoubleType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel(calculator::compute);
		return eigenvalues;
	}

	private static RandomAccessibleInterval<? extends GenericComposite<DoubleType>> collapse(
		List<RandomAccessibleInterval<DoubleType>> slices)
	{
		return Views.collapse(Views.stack(StackView.StackAccessMode.MOVE_ALL_SLICE_ACCESSES, slices));
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(EigenValueBenchmark.class.getSimpleName())
			.build();
		new Runner(options).run();
	}
}
