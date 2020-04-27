
package net.imglib2.trainable_segmention.gpu;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.trainable_segmention.gpu.compute_cache.GpuComputeCache;
import net.imglib2.trainable_segmention.gpu.compute_cache.GpuDerivativeContent;
import net.imglib2.trainable_segmention.gpu.compute_cache.GpuGaussContent;
import net.imglib2.trainable_segmention.gpu.compute_cache.GpuOriginalContent;
import net.imglib2.trainable_segmention.gpu.compute_cache.GpuSecondDerivativeContent;
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
