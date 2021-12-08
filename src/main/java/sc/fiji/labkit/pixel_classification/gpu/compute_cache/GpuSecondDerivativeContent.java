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

package sc.fiji.labkit.pixel_classification.gpu.compute_cache;

import sc.fiji.labkit.pixel_classification.gpu.api.GpuPixelWiseOperation;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.util.Intervals;

import java.util.Objects;

public class GpuSecondDerivativeContent implements GpuComputeCache.Content {

	private final GpuComputeCache cache;
	private final GpuComputeCache.Content input;
	private final int d;

	public GpuSecondDerivativeContent(GpuComputeCache cache, GpuComputeCache.Content input, int d) {
		this.cache = cache;
		this.input = input;
		this.d = d;
	}

	@Override
	public int hashCode() {
		return Objects.hash(input, d);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GpuSecondDerivativeContent &&
			input.equals(((GpuSecondDerivativeContent) obj).input) &&
			d == ((GpuSecondDerivativeContent) obj).d;
	}

	@Override
	public void request(Interval interval) {
		cache.request(input, requiredInput(interval));
	}

	private FinalInterval requiredInput(Interval interval) {
		return expand(interval, 1, d);
	}

	private static FinalInterval expand(Interval interval, int border, int d) {
		long[] b = new long[interval.numDimensions()];
		b[d] = border;
		return Intervals.expand(interval, b);
	}

	@Override
	public GpuImage load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		double[] pixelSize = cache.pixelSize();
		GpuView source = cache.get(input, requiredInput(interval));
		FinalInterval centerInterval = expand(new FinalInterval(source.dimensions()), -1, d);
		GpuView center = GpuViews.crop(source, centerInterval);
		GpuView front = GpuViews.crop(source, Intervals.translate(centerInterval, 1, d));
		GpuView back = GpuViews.crop(source, Intervals.translate(centerInterval, -1, d));
		GpuImage result = gpu.create(Intervals.dimensionsAsLongArray(interval), NativeTypeEnum.Float);
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("f", front)
			.addInput("b", back)
			.addInput("c", center)
			.addOutput("r", result)
			.addInput("factor", (float) (1 / Math.pow(pixelSize[d], 2)))
			.forEachPixel("r = (f + b - 2 * c) * factor");
		return result;
	}
}
