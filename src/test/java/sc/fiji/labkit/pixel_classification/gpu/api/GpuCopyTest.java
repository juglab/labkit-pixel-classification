/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link GpuCopy}
 */
public class GpuCopyTest extends AbstractGpuTest {

	@Test
	public void testCopy() {
		GpuImage source = gpu.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 3, 3));
		GpuImage destination = gpu.push(ArrayImgs.floats(new float[9], 3, 3));
		GpuView sourceView = GpuViews.crop(source, Intervals.createMinSize(0, 1, 2, 2));
		GpuView destinationView = GpuViews.crop(destination, Intervals.createMinSize(1, 0, 2, 2));
		GpuCopy.copyFromTo(gpu, sourceView, destinationView);
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(destination);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 0, 4, 5, 0, 7, 8,
			0, 0, 0 }, 3, 3);
		ImgLib2Assert.assertImageEquals(expected, result);
	}

	@Test
	public void testCopyToRai() {
		GpuImage source = gpu.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 3, 3));
		RandomAccessibleInterval<FloatType> target = ArrayImgs.floats(3, 3);
		GpuCopy.copyFromTo(source, target);
	}
}
