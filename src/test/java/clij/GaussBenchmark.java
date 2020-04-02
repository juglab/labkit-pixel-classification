
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJMultiChannelImage;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.ComputeCache;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.GaussContent;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
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

	private final FinalInterval interval = new FinalInterval(64, 64, 64);
	private final ClearCLBuffer output = clij.create(new long[] { 64, 64, 64 });
	private final RandomAccessible<FloatType> input = Utils.dirac(3);
	private final ComputeCache cache = new ComputeCache(clij, input, new double[] { 1, 1, 1 });
	private final GaussContent content = new GaussContent(cache, 8);

	@Setup
	public void setUp() {
		content.request(interval);
	}

	@TearDown
	public void tearDown() {
		output.close();
		clij.clear();
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkGauss() {
		return clij.pullRAI(content.load(interval));
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GaussBenchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}
}
