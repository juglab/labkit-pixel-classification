
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJMultiChannelImage;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class GaussBenchmark {

	private final CLIJ2 clij = CLIJ2.getInstance();

	private final ClearCLBuffer kernel = clij.push(RandomImgs.seed(42).nextImage(new FloatType(), 50,
		1));
	private final ClearCLBuffer output = clij.create(new long[] { 100, 100, 100 });
	private final ClearCLBuffer input = clij.push(RandomImgs.seed(42).nextImage(new FloatType(), 100 +
		kernel.getWidth() - 1, 100, 100));
	private final CLIJMultiChannelImage derivatives = new CLIJMultiChannelImage(clij, new long[] { 98,
		98, 98 }, 6);

	@TearDown
	public void tearDown() {
		input.close();
		kernel.close();
		output.close();
		clij.clear();
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkGauss() {
		for (int i = 0; i < 10; i++)
			Gauss.convolve(clij, input, kernel, output);
		return clij.pullRAI(output);
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkDerivatives() {
		CLIJView myOutput = CLIJView.shrink(output, 1);
		CLIJView myInput = CLIJView.interval(input, myOutput.interval());
		List<CLIJView> channels = derivatives.channels();
		for (int i = 0; i < 100; i++) {
			derivative(myInput, channels.get(0), 0);
			derivative(myInput, channels.get(1), 1);
			derivative(myInput, channels.get(2), 2);
			secondDerivative(myInput, channels.get(3), 0);
			secondDerivative(myInput, channels.get(4), 0);
			secondDerivative(myInput, channels.get(5), 0);
		}
		return derivatives.asRAI();
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkDerivativeCombinedKernel() {
		for (int i = 0; i < 100; i++) {
			CLIJView myOutput = CLIJView.shrink(output, 1);
			CLIJView myInput = CLIJView.interval(input, myOutput.interval());
			List<CLIJView> channels = derivatives.channels();
			CLIJView back0 = CLIJView.interval(myInput.buffer(), Intervals.translate(myInput.interval(),
				-1, 0));
			CLIJView front0 = CLIJView.interval(myInput.buffer(), Intervals.translate(myInput.interval(),
				+1, 0));
			CLIJView back1 = CLIJView.interval(myInput.buffer(), Intervals.translate(myInput.interval(),
				-1, 1));
			CLIJView front1 = CLIJView.interval(myInput.buffer(), Intervals.translate(myInput.interval(),
				+1, 1));
			CLIJView back2 = CLIJView.interval(myInput.buffer(), Intervals.translate(myInput.interval(),
				-1, 2));
			CLIJView front2 = CLIJView.interval(myInput.buffer(), Intervals.translate(myInput.interval(),
				+1, 2));
			CLIJLoopBuilder.clij(clij)
				.addInput("b0", back0).addInput("f0", front0).addOutput("r0", channels.get(0))
				.addInput("b1", back1).addInput("f1", front1).addOutput("r1", channels.get(1))
				.addInput("b2", back2).addInput("f2", front2).addOutput("r2", channels.get(2))
				.addInput("c", myInput)
				.addOutput("s0", channels.get(3))
				.addOutput("s1", channels.get(4))
				.addOutput("s2", channels.get(5))
				.forEachPixel("r0 = f0 - b0; r1 = f1 - b1; r2 = f2 - b2;" +
					" s0 = f0 + b0 - 2 * c;" +
					" s1 = f1 + b1 - 2 * c;" +
					" s2 = f2 + b2 - 2 * c;");
		}
		return derivatives.asRAI();
	}

	private void derivative(CLIJView input, CLIJView output, int d) {
		CLIJView back = CLIJView.interval(input.buffer(), Intervals.translate(input.interval(), -1, d));
		CLIJView front = CLIJView.interval(input.buffer(), Intervals.translate(input.interval(), +1,
			d));
		CLIJLoopBuilder.clij(clij).addInput("b", back).addInput("f", front).addOutput("r", output)
			.forEachPixel("r = f - b");
	}

	private void secondDerivative(CLIJView input, CLIJView output, int d) {
		CLIJView back = CLIJView.interval(input.buffer(), Intervals.translate(input.interval(), -1, d));
		CLIJView front = CLIJView.interval(input.buffer(), Intervals.translate(input.interval(), +1,
			d));
		CLIJLoopBuilder.clij(clij)
			.addInput("b", back)
			.addInput("c", input)
			.addInput("f", front)
			.addOutput("r", output)
			.forEachPixel("r = f + b - 2 * c");
	}

	// @Benchmark
	public RandomAccessibleInterval benchmarkEqualImages() {
		for (int j = 0; j < 10; j++) {
			CLIJView output = CLIJView.wrap(this.output);
			CLIJView input = CLIJView.interval(this.input, output.interval());
			String operation = "out = 0";
			CLIJLoopBuilder loopBuilder = CLIJLoopBuilder.clij(clij).addOutput("out", output);
			for (int i = 0; i < 27; i++) {
				String variable = "image" + i;
				loopBuilder.addInput(variable, input);
				operation += " + " + variable;
			}
			loopBuilder.forEachPixel(operation);
		}
		return derivatives.asRAI();
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GaussBenchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}
}
