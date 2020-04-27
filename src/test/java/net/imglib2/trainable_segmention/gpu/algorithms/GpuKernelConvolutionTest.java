
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.gpu.api.AbstractGpuTest;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuPool;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assume.assumeTrue;

/**
 * Tests {@link GpuKernelConvolution}.
 */
public class GpuKernelConvolutionTest extends AbstractGpuTest {

	@Test
	public void test() {
		try (
			GpuImage input = gpu.push(img1D(0, 0, 1, 0, 0));
			GpuImage kernel = gpu.push(img1D(-0.5f, 0, 0.5f));
			GpuImage output = gpu.create(new long[] { 3, 1 }, NativeTypeEnum.Float);)
		{
			GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(input), GpuViews.wrap(output), 0);
			ImgLib2Assert.assertImageEqualsRealType(img1D(0.5f, 0, -0.5f), gpu.pullRAI(output), 0);
		}
	}

	@Test
	public void test8() {
		try (
			GpuImage input = gpu.push(img1D(0, 1, 0));
			GpuImage kernel = gpu.push(img1D(-0.5f, 0, 0.5f));
			GpuImage output = gpu.create(new long[] { 3, 1 }, NativeTypeEnum.Float);)
		{
			GpuSeparableOperation.convolve(gpu, kernel, input, 1, output, 0);
			RandomAccessibleInterval actual = gpu.pullRAI(output);
			ImgLib2Assert.assertImageEqualsRealType(img1D(0.5f, 0, -0.5f), actual, 0);
		}
	}

	@Test
	public void testBorder() {
		try (
			GpuImage input = gpu.push(img1D(2, 0, 0, 0, 3));
			GpuImage kernel = gpu.push(img1D(-0.5f, 0, 0.5f));
			GpuImage output = gpu.create(new long[] { 3, 1 }, NativeTypeEnum.Float);)
		{
			GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(input), GpuViews.wrap(output), 0);
			ImgLib2Assert.assertImageEqualsRealType(img1D(-1, 0, 1.5f), gpu.pullRAI(output), 0);
		}
	}

	@Ignore("FIXME")
	@Test
	public void testLongLine() {
		int length = 10000;
		try (
			GpuImage input = gpu.push(img1D(new float[length + 2]));
			GpuImage kernel = gpu.push(img1D(-0.5f, 0, 0.5f));
			GpuImage output = gpu.create(new long[] { length, 1 }, NativeTypeEnum.Float);)
		{
			GpuKernelConvolution.convolve(gpu, kernel, GpuViews.wrap(input), GpuViews.wrap(output), 0);
			ImgLib2Assert.assertImageEqualsRealType(img1D(new float[length]), gpu.pullRAI(output), 0);
		}
	}

	@Test
	public void testY() {
		try (
			GpuImage input = gpu.push(ArrayImgs.floats(new float[] {
				0, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, -1, 0
			}, 4, 3));
			GpuImage kernel = gpu.push(img1D(1, 2));
			GpuImage output = gpu.create(new long[] { 3, 2 }, NativeTypeEnum.Float);)
		{
			GpuKernelConvolution.convolve(gpu, kernel, GpuViews.crop(input, FinalInterval.createMinSize(1,
				0, 2, 2)), GpuViews.wrap(output), 1);
			RandomAccessibleInterval actual = gpu.pullRAI(output);
			ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] {
				2, 0, 0,
				1, -2, 0
			}, 3, 2), actual, 0);
		}
	}

	private ArrayImg<FloatType, FloatArray> img1D(float... array) {
		return ArrayImgs.floats(array, array.length, 1);
	}
}
