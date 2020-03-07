
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class GaussBenchmark {

	private final CLIJ2 clij = CLIJ2.getInstance();

	private final ClearCLBuffer input = clij.push(RandomImgs.seed(42).nextImage(new FloatType(), 100,
		100, 100));
	private final ClearCLBuffer kernel = clij.push(RandomImgs.seed(42).nextImage(new FloatType(), 11,
		1));
	private final ClearCLBuffer output = clij.create(new long[] { 90, 100, 100 });

	@TearDown
	public void tearDown() {
		input.close();
		kernel.close();
		output.close();
		clij.clear();
	}

	@Benchmark
	public void benchmark() {
		for (int i = 0; i < 10; i++)
			Gauss.convolve(clij, input, kernel, output);
		clij.pullRAI(output);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GaussBenchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}
}
