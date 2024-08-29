/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.gpu.api.AbstractGpuTest;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.util.HashMap;

public class GpuSeparableOperationTest extends AbstractGpuTest {

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
