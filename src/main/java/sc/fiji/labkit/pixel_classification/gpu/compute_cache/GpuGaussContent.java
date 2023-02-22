/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

import sc.fiji.labkit.pixel_classification.gpu.algorithms.GpuGauss;
import sc.fiji.labkit.pixel_classification.gpu.algorithms.GpuNeighborhoodOperation;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.Interval;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.util.Intervals;

import java.util.stream.DoubleStream;

public class GpuGaussContent implements GpuComputeCache.Content {

	private final GpuComputeCache cache;

	private final double sigma;

	private final GpuOriginalContent source;

	private final GpuNeighborhoodOperation operation;

	public GpuGaussContent(GpuComputeCache cache, double sigma) {
		this.cache = cache;
		this.sigma = sigma;
		this.source = new GpuOriginalContent(cache);
		this.operation = GpuGauss.gauss(cache.gpuApi(), sigmas(cache.pixelSize()));
	}

	@Override
	public int hashCode() {
		return Double.hashCode(sigma);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GpuGaussContent &&
			source.equals(((GpuGaussContent) obj).source) &&
			sigma == ((GpuGaussContent) obj).sigma;
	}

	@Override
	public void request(Interval interval) {
		cache.request(source, operation.getRequiredInputInterval(interval));
	}

	@Override
	public GpuImage load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		GpuView original = cache.get(source, operation.getRequiredInputInterval(interval));
		GpuImage output = gpu.create(Intervals.dimensionsAsLongArray(interval), NativeTypeEnum.Float);
		operation.apply(original, GpuViews.wrap(output));
		return output;
	}

	private double[] sigmas(double[] pixelSize) {
		return DoubleStream.of(pixelSize).map(p -> sigma / p).toArray();
	}
}
