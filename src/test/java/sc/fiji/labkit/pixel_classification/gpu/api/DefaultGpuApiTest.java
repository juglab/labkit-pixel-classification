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

package sc.fiji.labkit.pixel_classification.gpu.api;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Test {@link DefaultGpuApi}.
 */
public class DefaultGpuApiTest {

	private GpuApi gpu;

	@Before
	public void before() {
		assumeTrue(DefaultGpuApi.isDeviceAvailable());
		gpu = new DefaultGpuApi(0);
	}

	@After
	public void after() {
		if (gpu != null)
			gpu.close();
	}

	@Test
	public void testCreate() {
		GpuImage image = gpu.create(new long[] { 10, 20 }, 3, NativeTypeEnum.Byte);
		assertEquals(10, image.getWidth());
		assertEquals(20, image.getHeight());
		assertEquals(1, image.getDepth());
		assertEquals(3, image.getNumberOfChannels());
		assertEquals(NativeTypeEnum.Byte, image.getNativeType());
	}

	@Test
	public void testBufferReuse() {
		ClearCLBuffer aBuffer, bBuffer;
		try (GpuApi scope = gpu.subScope()) {
			GpuImage a = scope.create(new long[] { 10, 10 }, NativeTypeEnum.Float);
			aBuffer = a.clearCLBuffer();
		}
		try (GpuApi scope = gpu.subScope()) {
			GpuImage b = scope.create(new long[] { 10, 10 }, NativeTypeEnum.Float);
			bBuffer = b.clearCLBuffer();
		}
		assertSame(aBuffer, bBuffer);
	}

	@Test
	public void testPushAndPull() {
		RandomAccessibleInterval<FloatType> image = ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6 },
			2, 3);
		try (GpuImage gpuImage = gpu.push(image)) {
			RandomAccessibleInterval<FloatType> result = gpu.pullRAI(gpuImage);
			ImgLib2Assert.assertImageEquals(image, result);
		}
	}

	@Test
	public void testPushAndPullMultiChannel() {
		RandomAccessibleInterval<FloatType> image = ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6 },
			2, 1, 3);
		try (GpuImage gpuImage = gpu.pushMultiChannel(image)) {
			assertArrayEquals(new long[] { 2, 1 }, gpuImage.getDimensions());
			assertEquals(3, gpuImage.getNumberOfChannels());
			RandomAccessibleInterval<FloatType> result = gpu.pullRAIMultiChannel(gpuImage);
			ImgLib2Assert.assertImageEquals(image, result);
		}
	}
}
