package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import preview.net.imglib2.loops.LoopBuilder;
import preview.net.imglib2.loops.LoopUtils;
import preview.net.imglib2.loops.SyncedPositionables;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(0)
public class CollapseBenchmark {

	private final RandomAccessibleInterval< DoubleType >
			input = RandomImgs.seed(42).randomize(new ArrayImgFactory<>(new DoubleType()).create(100, 100, 100, 3));
	private final Img< DoubleType > output = ArrayImgs.doubles(100, 100, 100);

	@Benchmark
	public void benchmarkViewsCollapse() {
		int n = (int) input.dimension(input.numDimensions() - 1);
		LoopBuilder.setImages(Views.collapse(input), output).forEachPixel((a, b) -> {
			double sum = 0;
			for (int i = 0; i < n; i++) {
				sum += a.get(i).getRealDouble();
			}
			b.setReal(sum);
		});
	}

	@Benchmark
	public void benchmarkFastViewsCollapse() {
		int n = (int) input.dimension(input.numDimensions() - 1);
		LoopBuilder.setImages(FastViews.collapse(input), output).forEachPixel((a, b) -> {
			double sum = 0;
			for (int i = 0; i < n; i++) {
				sum += a.get(i).getRealDouble();
			}
			b.setReal(sum);
		});
	}

	@Benchmark
	public void benchmarkHyperSlice() {
		int d = input.numDimensions() - 1;
		LoopBuilder.setImages(Views.hyperSlice(input, d, 0), Views.hyperSlice(input, d, 1), Views.hyperSlice(input, d, 2), output).forEachPixel((a, b, c, o) -> {
			o.setReal(a.getRealDouble() + b.getRealDouble() + c.getRealDouble());
		});
	}

	@Benchmark
	public void benchmarkLoopUtils() {
		int d = input.numDimensions() - 1;
		int n = (int) input.dimension(d);
		RandomAccess< DoubleType > a = input.randomAccess();
		RandomAccess< DoubleType > b = output.randomAccess();
		LoopUtils.createIntervalLoop(SyncedPositionables.create(a, b), output, () -> {
			double sum = 0;
			a.setPosition(0, d);
			for (int i = 0; i < n - 1; i++) {
				sum += a.get().getRealDouble();
				a.fwd(d);
			}
			sum += a.get().getRealDouble();
			b.get().setReal(sum);
		}).run();
	}

	@Benchmark
	public void benchmarkMinOverheadForComposite() {
		int d = input.numDimensions() - 1;
		int n = (int) input.dimension(d);
		RandomAccess< DoubleType > a = input.randomAccess();
		Composite< DoubleType > composite = i -> {
			a.fwd(d);
			return a.get();
		};
		RandomAccess< DoubleType > b = output.randomAccess();
		LoopUtils.createIntervalLoop(SyncedPositionables.create(a, b), output, () -> {
			double sum = 0;
			a.setPosition(-1, d);
			for (int i = 0; i < n; i++) {
				sum += composite.get(i).getRealDouble();
			}
			b.get().setReal(sum);
		}).run();
	}

	@Benchmark
	public void benchmarkMinOverheadForComposite2() {
		int d = input.numDimensions() - 1;
		int n = (int) input.dimension(d);
		RandomAccess< DoubleType >[] a = new RandomAccess[]{
				input.randomAccess(), input.randomAccess(), input.randomAccess()};
		a[1].setPosition(1, d);
		a[2].setPosition(2, d);
		Composite<DoubleType> composite = i -> a[(int) i].get();
		RandomAccess< DoubleType > b = output.randomAccess();
		LoopUtils.createIntervalLoop(SyncedPositionables.create(a[0], a[1], a[2], b), output, () -> {
			double sum = 0;
			for (int i = 0; i < n; i++) {
				sum += composite.get(i).getRealDouble();
			}
			b.get().setReal(sum);
		}).run();
	}

	@Benchmark
	public void benchmarkWrappedRandomAccess() {
		int d = input.numDimensions() - 1;
		int n = (int) input.dimension(d);
		RandomAccessibleInterval< RandomAccess< DoubleType > > composite = SimpleRAI.create(new WrappedRandomAccess<>(input.randomAccess()), output);
		LoopBuilder.setImages(composite, output).forEachPixel((in, out) -> {
			double sum = 0;
			in.setPosition(0, d);
			for (int i = 0; i < n; i++) {
				sum += in.get().getRealDouble();
				in.fwd(d);
			}
			out.setReal(sum);
		});
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(CollapseBenchmark.class.getSimpleName())
				.build();
		new Runner(options).run();
	}

}
