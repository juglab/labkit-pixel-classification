
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.trainable_segmention.utils.ToString;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;

public class GpuSeparableOperationTest {

	private final GpuApi gpu = GpuApi.getInstance();

	@After
	public void after() {
		gpu.close();
	}

	@Test
	public void test() {
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 2, 0, 0,
			0, 0, 0, 1, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
		}, 7, 5);
		Img<FloatType> expected = ArrayImgs.floats(new float[] {
			0, 0, 2, 2, 2,
			0, 1, 2, 2, 2,
			0, 1, 2, 2, 2,
		}, 5, 3);
		GpuImage in = gpu.push(input);
		GpuImage out = gpu.create(new long[] { 5, 3 }, NativeTypeEnum.Float);
		GpuNeighborhoodOperation operation = GpuNeighborhoodOperations.max(gpu, new int[] { 3, 3 });
		operation.apply(GpuViews.wrap(in), GpuViews.wrap(out));
		ImgLib2Assert.assertImageEquals(expected, gpu.pullRAI(out));
	}

	@Test
	public void testOpenCLKernelMax() {
		Img<FloatType> inputImg = ArrayImgs.floats(new float[] {
			0, 2, 0, 3, 0,
			0, 1, 0, 0, 0,
		}, 5, 2);
		Img<FloatType> expected = ArrayImgs.floats(new float[] {
			2, 3, 3,
			1, 1, 0,
		}, 3, 2);
		testOpenCLKernel(inputImg, expected, "max1d.cl");
	}

	@Test
	public void testOpenCLKernelMin() {
		Img<FloatType> inputImg = ArrayImgs.floats(new float[] {
			0, -2, 0, -3, 0,
			0, -1, 0, 0, 0,
		}, 5, 2);
		Img<FloatType> expected = ArrayImgs.floats(new float[] {
			-2, -3, -3,
			-1, -1, -0,
		}, 3, 2);
		testOpenCLKernel(inputImg, expected, "min1d.cl");
	}

	@Test
	public void testOpenCLKernelMean() {
		Img<FloatType> inputImg = ArrayImgs.floats(new float[] {
			0, 6, 0, 3, 0,
			0, -3, 0, 0, 0,
		}, 5, 2);
		Img<FloatType> expected = ArrayImgs.floats(new float[] {
			2, 3, 1,
			-1, -1, 0,
		}, 3, 2);
		testOpenCLKernel(inputImg, expected, "mean1d.cl");
	}

	private void testOpenCLKernel(Img<FloatType> inputImg, Img<FloatType> expected,
		String kernelName)
	{
		GpuImage input = gpu.push(inputImg);
		GpuImage output = gpu.create(new long[] { 3, 2 }, NativeTypeEnum.Float);
		GpuSeparableOperation.run(gpu, kernelName, 3, new HashMap<>(), GpuViews.wrap(input), GpuViews
			.wrap(output), 0);
		ImgLib2Assert.assertImageEquals(expected, gpu.pullRAI(output));
	}

}
