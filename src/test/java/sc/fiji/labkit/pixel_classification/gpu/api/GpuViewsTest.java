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

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.util.Intervals;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link GpuViews}.
 */
public class GpuViewsTest extends AbstractGpuTest {

	@Test
	public void testWrap() {
		GpuImage image = gpu.create(new long[] { 2, 3 }, NativeTypeEnum.Float);
		GpuView view = GpuViews.wrap(image);
		assertArrayEquals(new long[] { 2, 3 }, Intervals.dimensionsAsLongArray(view.dimensions()));
		assertSame(image, view.source());
		assertEquals(0, view.offset());
	}

	@Test
	public void testCrop() {
		GpuImage image = gpu.create(new long[] { 6, 4 }, NativeTypeEnum.Float);
		GpuView view = GpuViews.crop(image, FinalInterval.createMinSize(1, 2, 3, 1));
		assertArrayEquals(new long[] { 3, 1 }, Intervals.dimensionsAsLongArray(view.dimensions()));
		assertSame(image, view.source());
		assertEquals(13, view.offset());
	}

	@Test
	public void testChannel() {
		GpuImage image = gpu.create(new long[] { 6, 4 }, 2, NativeTypeEnum.Float);
		List<GpuView> channels = GpuViews.channels(image);
		GpuView channel = channels.get(1);
		assertEquals(2, channels.size());
		assertArrayEquals(new long[] { 6, 4 }, Intervals.dimensionsAsLongArray(channel.dimensions()));
		assertSame(image, channel.source());
		assertEquals(24, channel.offset());
	}
}
