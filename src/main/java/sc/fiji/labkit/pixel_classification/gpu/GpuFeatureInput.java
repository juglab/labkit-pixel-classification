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

package sc.fiji.labkit.pixel_classification.gpu;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.compute_cache.GpuComputeCache;
import sc.fiji.labkit.pixel_classification.gpu.compute_cache.GpuDerivativeContent;
import sc.fiji.labkit.pixel_classification.gpu.compute_cache.GpuGaussContent;
import sc.fiji.labkit.pixel_classification.gpu.compute_cache.GpuOriginalContent;
import sc.fiji.labkit.pixel_classification.gpu.compute_cache.GpuSecondDerivativeContent;
import net.imglib2.type.numeric.real.FloatType;

public class GpuFeatureInput {

	private final GpuApi gpu;

	private final Interval targetInterval;

	private final GpuComputeCache cache;

	public GpuFeatureInput(GpuApi gpu, RandomAccessible<FloatType> original, Interval targetInterval,
		double[] pixelSize)
	{
		this.gpu = gpu;
		this.targetInterval = targetInterval;
		this.cache = new GpuComputeCache(gpu, original, pixelSize);
	}

	public void prefetchOriginal(Interval interval) {
		cache.request(new GpuOriginalContent(cache), interval);
	}

	public GpuView original(Interval interval) {
		return cache.get(new GpuOriginalContent(cache), interval);
	}

	public void prefetchGauss(double sigma, Interval interval) {
		cache.request(gaussKey(sigma), interval);
	}

	public GpuView gauss(double sigma, Interval interval) {
		return cache.get(gaussKey(sigma), interval);
	}

	public void prefetchDerivative(double sigma, int dimension, Interval interval) {
		cache.request(new GpuDerivativeContent(cache, gaussKey(sigma), dimension), interval);
	}

	public GpuView derivative(double sigma, int dimension, Interval interval) {
		return cache.get(new GpuDerivativeContent(cache, gaussKey(sigma), dimension), interval);
	}

	public void prefetchSecondDerivative(double sigma, int dimensionA, int dimensionB,
		Interval interval)
	{
		cache.request(secondDerivativeKey(sigma, dimensionA, dimensionB), interval);
	}

	public GpuView secondDerivative(double sigma, int dimensionA, int dimensionB, Interval interval) {
		return cache.get(secondDerivativeKey(sigma, dimensionA, dimensionB), interval);
	}

	private GpuGaussContent gaussKey(double sigma) {
		return new GpuGaussContent(cache, sigma);
	}

	private GpuComputeCache.Content secondDerivativeKey(double sigma, int dimensionA,
		int dimensionB)
	{
		if (dimensionA > dimensionB)
			return secondDerivativeKey(sigma, dimensionB, dimensionA);
		if (dimensionA == dimensionB)
			return new GpuSecondDerivativeContent(cache, gaussKey(sigma), dimensionA);
		return new GpuDerivativeContent(cache, new GpuDerivativeContent(cache, gaussKey(sigma),
			dimensionA), dimensionB);
	}

	public Interval targetInterval() {
		return targetInterval;
	}

	public GpuApi gpuApi() {
		return gpu;
	}
}
