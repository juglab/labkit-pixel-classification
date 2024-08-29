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

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class GpuViews {

	public static GpuView wrap(GpuImage buffer) {
		return new GpuView(buffer, new FinalDimensions(buffer.getDimensions()), 0);
	}

	public static GpuView crop(GpuImage image, Interval interval) {
		long[] dimensions = image.getDimensions();
		long offset = IntervalIndexer.positionToIndex(Intervals.minAsLongArray(interval), dimensions);
		return new GpuView(image, new FinalDimensions(interval), offset);
	}

	public static GpuView crop(GpuView image, Interval interval) {
		GpuImage source = image.source();
		long[] dimensions = source.getDimensions();
		long offset = IntervalIndexer.positionToIndex(Intervals.minAsLongArray(interval), dimensions);
		return new GpuView(source, new FinalDimensions(interval), offset + image.offset());
	}

	public static GpuView shrink(GpuImage image, long[] border) {
		long[] negativeBorder = LongStream.of(border).map(x -> -x).toArray();
		return crop(image, Intervals.expand(new FinalInterval(image.getDimensions()), negativeBorder));
	}

	public static GpuView channel(GpuImage image, long position) {
		if ((position < 0) || (position >= image.getNumberOfChannels()))
			throw new IllegalArgumentException();
		return new GpuView(image,
			new FinalDimensions(image.getDimensions()),
			Intervals.numElements(image.getDimensions()) * position);
	}

	public static GpuImage asGpuImage(GpuApi gpu, GpuView view) {
		GpuImage buffer = gpu.create(Intervals.dimensionsAsLongArray(view.dimensions()),
			NativeTypeEnum.Float);
		GpuCopy.copyFromTo(gpu, view, wrap(buffer));
		return buffer;
	}

	public static List<GpuView> channels(GpuImage buffer) {
		return LongStream.range(0, buffer.getNumberOfChannels())
			.mapToObj(i -> channel(buffer, i))
			.collect(Collectors.toList());
	}
}
