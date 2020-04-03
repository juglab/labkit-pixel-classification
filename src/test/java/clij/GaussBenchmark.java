
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.converters.implementations.RandomAccessibleIntervalToClearCLBufferConverter;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.ComputeCache;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.GaussContent;
import net.imglib2.type.numeric.real.FloatType;
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
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class GaussBenchmark {

	private final CLIJ2 clij = CLIJ2.getInstance();
	private final ClearCLBufferReuse buffers = new ClearCLBufferReuse(clij);

	private final FinalInterval interval = new FinalInterval(64, 64, 64);
	private final ClearCLBuffer inputBuffer = clij.push(RandomImgs.seed(1).nextImage(new FloatType(),
		interval));
	private final ClearCLBuffer output = clij.create(new long[] { 64, 64, 64 });
	private final RandomAccessible<FloatType> input = Utils.dirac(3);
	private final ComputeCache cache = new ComputeCache(clij, input, new double[] { 1, 1, 1 });
	private final GaussContent content = new GaussContent(cache, 8);
	private final ClearCLBuffer kernel = gaussKernel(8);
	private final long large = 64 + kernel.getWidth() - 1;
	private final ClearCLBuffer inputBuffer2 = clij.push(RandomImgs.seed(1).nextImage(new FloatType(),
		large, large, large));
	private final ClearCLBuffer tmp1 = createNoAccessBuffer(64, large, large);
	private final ClearCLBuffer tmp2 = createNoAccessBuffer(64, 64, large);

	@Setup
	public void setUp() {
		clij.setKeepReferences(false);
		content.request(interval);
	}

	@TearDown
	public void tearDown() {
		output.close();
		inputBuffer.close();
		inputBuffer2.close();
		tmp1.close();
		tmp2.close();
		kernel.close();
		clij.clear();
		buffers.close();
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkClij2Gauss() {
		clij.gaussianBlur3D(inputBuffer, output, 8, 8, 8);
		return clij.pullRAI(output);
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkGauss() {
		try (ClearCLBuffer load = content.load(interval)) {
			return clij.pullRAI(load);
		}
	}

	@Benchmark
	public RandomAccessibleInterval intermediate() {
		try (ClearCLBuffer output = clij.create(64, 64, 64)) {
			NeighborhoodOperation gauss = Gauss.gauss(clij, 8, 8, 8);
			gauss.convolve(CLIJView.wrap(inputBuffer2), CLIJView.wrap(output));
			return clij.pullRAI(output);
		}
	}

	@Benchmark
	public RandomAccessibleInterval lowLevelGauss() {
		try (ReuseScope scope = new ReuseScope(buffers)) {
			ClearCLBuffer tmp1 = scope.create(64, large, large);
			ClearCLBuffer tmp2 = scope.create(64, 64, large);
			ClearCLBuffer output = scope.create(64, 64, 64);
			{
				ClearCLBuffer kernel = scope.add(gaussKernel(8));
				CLIJKernelConvolution.convolve(clij, CLIJView.wrap(inputBuffer2), kernel, CLIJView.wrap(
					tmp1), 0);
			}
			{
				ClearCLBuffer kernel = scope.add(gaussKernel(8));
				CLIJKernelConvolution.convolve(clij, CLIJView.wrap(tmp1), kernel, CLIJView.wrap(tmp2), 1);
			}
			{
				ClearCLBuffer kernel = scope.add(gaussKernel(8));
				CLIJKernelConvolution.convolve(clij, CLIJView.wrap(tmp2), kernel, CLIJView.wrap(output), 2);
			}
			return clij.pullRAI(output);
		}
	}

	@Benchmark
	public RandomAccessibleInterval lowLevelGaussReuseBuffers() {
		CLIJKernelConvolution.convolve(clij, CLIJView.wrap(inputBuffer2), kernel, CLIJView.wrap(tmp1),
			0);
		CLIJKernelConvolution.convolve(clij, CLIJView.wrap(tmp1), kernel, CLIJView.wrap(tmp2), 1);
		CLIJKernelConvolution.convolve(clij, CLIJView.wrap(tmp2), kernel, CLIJView.wrap(output), 2);
		return clij.pullRAI(output);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GaussBenchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}

	private ClearCLBuffer gaussKernel(double sigma) {
		double[] fullKernel = Kernel1D.symmetric(Gauss3.halfkernels(new double[] { sigma })[0])
			.fullKernel();
		ClearCLBuffer buffer = buffers.create(new long[] { fullKernel.length });
		RandomAccessibleIntervalToClearCLBufferConverter.copyRandomAccessibleIntervalToClearCLBuffer(
			ArrayImgs.doubles(fullKernel, fullKernel.length, 1),
			buffer);
		return buffer;
	}

	private ClearCLBuffer createNoAccessBuffer(long... dims) {
		return clij.getCLIJ().getClearCLContext().createBuffer(MemAllocMode.AllocateHostPointer,
			HostAccessType.NoAccess, KernelAccessType.ReadWrite, 1, NativeTypeEnum.Float, dims);
	}

}
