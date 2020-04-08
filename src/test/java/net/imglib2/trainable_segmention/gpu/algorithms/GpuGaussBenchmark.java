
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.converters.implementations.RandomAccessibleIntervalToClearCLBufferConverter;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.trainable_segmention.gpu.compute_cache.GpuComputeCache;
import net.imglib2.trainable_segmention.gpu.compute_cache.GpuGaussContent;
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
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import java.util.concurrent.TimeUnit;

@Fork(0)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class GpuGaussBenchmark {

	private final GpuApi gpu = GpuApi.getInstance();
	private final CLIJ2 clij2 = CLIJ2.getInstance();
	private final long[] dimessions = { 64, 64, 64 };
	private final FinalInterval interval = new FinalInterval(64, 64, 64);

	private final ClearCLBuffer inputBuffer = clij2.push(RandomImgs.seed(1).nextImage(new FloatType(),
		dimessions));
	private final ClearCLBuffer outputBuffer = clij2.create(dimessions);
	private final GpuImage output = gpu.create(dimessions, NativeTypeEnum.Float);
	private final RandomAccessible<FloatType> input = Utils.dirac(3);
	private final GpuComputeCache cache = new GpuComputeCache(gpu, input, new double[] { 1, 1, 1 });
	private final GpuGaussContent content = new GpuGaussContent(cache, 8);
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
		cache.close();
		gpu.close();
		inputBuffer.close();
		outputBuffer.close();
		clij2.close();
	}

	@Benchmark
	public RandomAccessibleInterval benchmarkClij2Gauss() {
		clij2.gaussianBlur3D(inputBuffer, outputBuffer, 8, 8, 8);
		return clij2.pullRAI(output);
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
			GpuNeighborhoodOperation gauss = GpuGauss.gauss(gpu, 8, 8, 8);
			gauss.apply(GpuViews.wrap(inputBuffer2), GpuViews.wrap(output));
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
				GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(inputBuffer2), GpuViews.wrap(tmp1),
					0);
			}
			{
				GpuImage kernel = scope.add(gaussKernel(8));
				GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(tmp1), GpuViews.wrap(tmp2), 1);
			}
			{
				GpuImage kernel = scope.add(gaussKernel(8));
				GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(tmp2), GpuViews.wrap(output), 2);
			}
			return gpu.pullRAI(output);
		}
	}

	@Benchmark
	public RandomAccessibleInterval lowLevelGaussReuseBuffers() {
		GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(inputBuffer2), GpuViews.wrap(tmp1), 0);
		GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(tmp1), GpuViews.wrap(tmp2), 1);
		GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(tmp2), GpuViews.wrap(output), 2);
		return gpu.pullRAI(output);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GpuGaussBenchmark.class.getSimpleName()).build();
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
