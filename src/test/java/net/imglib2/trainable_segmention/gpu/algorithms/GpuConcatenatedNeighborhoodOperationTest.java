
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.trainable_segmention.utils.ToString;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;

import java.util.Arrays;

/**
 * Tests {@link GpuConcatenatedNeighborhoodOperation}.
 */
public class GpuConcatenatedNeighborhoodOperationTest {

	@Test
	public void test() {
		GpuApi gpu = GpuApi.getInstance();
		Img<FloatType> dirac = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		GpuView input = GpuViews.wrap(gpu.push(dirac));
		GpuView output = GpuViews.wrap(gpu.create(new long[] { 3, 3 }, NativeTypeEnum.Float));
		GpuKernelConvolution a = new GpuKernelConvolution(gpu, Kernel1D.centralAsymmetric(1, 0, -1), 0);
		GpuKernelConvolution b = new GpuKernelConvolution(gpu, Kernel1D.centralAsymmetric(1, 2, 1), 1);
		GpuNeighborhoodOperation concatenation = GpuNeighborhoodOperations.concat(gpu, Arrays.asList(a,
			b));
		Interval inputInterval = concatenation.getRequiredInputInterval(Intervals.createMinMax(-1, -1,
			1, 1));
		Interval expectedInterval = Intervals.createMinMax(-2, -2, 2, 2);
		ImgLib2Assert.assertIntervalEquals(expectedInterval, inputInterval);
		concatenation.convolve(input, output);
		RandomAccessibleInterval<FloatType> rai = gpu.pullRAI(output.source());
		ToString.print(rai);
	}
}
