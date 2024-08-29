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
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.gpu.api.AbstractGpuTest;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPool;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
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
