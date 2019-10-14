
package preview.net.imglib2.algorithm.gauss3;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
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
import preview.net.imglib2.algorithm.convolution.fast_gauss.FastGauss;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class Gauss3Benchmark {

	private final double sigma = 10;
	private final int size = 300;
	private final int width = size;
	private final int height = size;
	private final int depth = size;
	private final RandomAccessibleInterval<FloatType> input = ArrayImgs.floats(width, height, depth);
	private final RandomAccessibleInterval<FloatType> output = ArrayImgs.floats(width, height, depth);
	private final ImagePlus imagePlus = NewImage.createFloatImage("", width, height, depth, 0);

	@Benchmark
	public void benchmarkGauss3() {
		Gauss3.gauss(sigma, Views.extendBorder(input), output);
	}

	@Benchmark
	public void benchmarkIJ1Gauss() {
		IJ.run(imagePlus, "Gaussian Blur 3D...", "sgima=" + sigma);
	}

	@Benchmark
	public void benchmarkFastGauss() {
		FastGauss.convolve(sigma, Views.extendBorder(input), output);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(Gauss3Benchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}

}
