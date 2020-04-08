
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.haesleinhuepf.clij.converters.implementations.RandomAccessibleIntervalToClearCLBufferConverter;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.trainable_segmention.gpu.compute_cache.ComputeCache;
import net.imglib2.trainable_segmention.gpu.compute_cache.GaussContent;
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

@Fork(0)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class GaussBenchmark {

	private final GpuApi gpu = GpuApi.getInstance();

	private final FinalInterval interval = new FinalInterval(64, 64, 64);
	private final GpuImage inputBuffer = gpu.push(RandomImgs.seed(1).nextImage(new FloatType(),
		interval));
	private final GpuImage output = gpu.create(new long[] { 64, 64, 64 }, NativeTypeEnum.Float);
	private final RandomAccessible<FloatType> input = Utils.dirac(3);
	private final ComputeCache cache = new ComputeCache(gpu, input, new double[] { 1, 1, 1 });
	private final GaussContent content = new GaussContent(cache, 8);
	private final GpuImage kernel = gaussKernel(8);
	private final long large = 64 + kernel.getWidth() - 1;
	private final GpuImage inputBuffer2 = gpu.push(RandomImgs.seed(1).nextImage(new FloatType(),
		large, large, large));
	private final GpuImage tmp1 = gpu.create(new long[] { 64, large, large }, NativeTypeEnum.Float);
	private final GpuImage tmp2 = gpu.create(new long[] { 64, 64, large }, NativeTypeEnum.Float);

	@Setup
	public void setUp() {
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
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkClij2Gauss() {
		gpu.gaussianBlur3D(inputBuffer, output, 8, 8, 8);
		return gpu.pullRAI(output);
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkGauss() {
		try (GpuImage load = content.load(interval)) {
			return gpu.pullRAI(load);
		}
	}

	@Benchmark
	public RandomAccessibleInterval intermediate() {
		try (GpuImage output = gpu.create(new long[] { 64, 64, 64 }, NativeTypeEnum.Float)) {
			NeighborhoodOperation gauss = Gauss.gauss(gpu, 8, 8, 8);
			gauss.convolve(GpuViews.wrap(inputBuffer2), GpuViews.wrap(output));
			return gpu.pullRAI(output);
		}
	}

	@Benchmark
	public RandomAccessibleInterval lowLevelGauss() {
		try (GpuScope scope = new GpuScope(gpu)) {
			GpuImage tmp1 = scope.create(64, large, large);
			GpuImage tmp2 = scope.create(64, 64, large);
			GpuImage output = scope.create(64, 64, 64);
			{
				GpuImage kernel = scope.add(gaussKernel(8));
				CLIJKernelConvolution.convolve(gpu, GpuViews.wrap(inputBuffer2), kernel, GpuViews.wrap(
					tmp1), 0);
			}
			{
				GpuImage kernel = scope.add(gaussKernel(8));
				CLIJKernelConvolution.convolve(gpu, GpuViews.wrap(tmp1), kernel, GpuViews.wrap(tmp2), 1);
			}
			{
				GpuImage kernel = scope.add(gaussKernel(8));
				CLIJKernelConvolution.convolve(gpu, GpuViews.wrap(tmp2), kernel, GpuViews.wrap(output), 2);
			}
			return gpu.pullRAI(output);
		}
	}

	@Benchmark
	public RandomAccessibleInterval lowLevelGaussReuseBuffers() {
		CLIJKernelConvolution.convolve(gpu, GpuViews.wrap(inputBuffer2), kernel, GpuViews.wrap(tmp1),
			0);
		CLIJKernelConvolution.convolve(gpu, GpuViews.wrap(tmp1), kernel, GpuViews.wrap(tmp2), 1);
		CLIJKernelConvolution.convolve(gpu, GpuViews.wrap(tmp2), kernel, GpuViews.wrap(output), 2);
		return gpu.pullRAI(output);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GaussBenchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}

	private GpuImage gaussKernel(double sigma) {
		double[] fullKernel = Kernel1D.symmetric(Gauss3.halfkernels(new double[] { sigma })[0])
			.fullKernel();
		GpuImage buffer = gpu.create(new long[] { fullKernel.length }, NativeTypeEnum.Float);
		RandomAccessibleIntervalToClearCLBufferConverter.copyRandomAccessibleIntervalToClearCLBuffer(
			ArrayImgs.doubles(fullKernel, fullKernel.length, 1),
			buffer.clearCLBuffer());
		return buffer;
	}
}
