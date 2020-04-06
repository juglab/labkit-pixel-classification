
package net.imglib2.trainable_segmention.clij_random_forest;

import clij.GpuApi;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.ComputeCache;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.DerivativeContent;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.GaussContent;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.OriginalContent;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.SecondDerivativeContent;
import net.imglib2.type.numeric.real.FloatType;

public class CLIJFeatureInput implements AutoCloseable {

	private final GpuApi gpu;
	private final Interval targetInterval;

	private final ComputeCache cache;

	public CLIJFeatureInput(GpuApi gpu, RandomAccessible<FloatType> original, Interval targetInterval,
		double[] pixelSize)
	{
		this.gpu = gpu;
		this.targetInterval = targetInterval;
		this.cache = new ComputeCache(gpu, original, pixelSize);
	}

	public void prefetchOriginal(Interval interval) {
		cache.request(new OriginalContent(cache), interval);
	}

	public GpuView original(Interval interval) {
		return cache.get(new OriginalContent(cache), interval);
	}

	public void prefetchGauss(double sigma, Interval interval) {
		cache.request(gaussKey(sigma), interval);
	}

	public GpuView gauss(double sigma, Interval interval) {
		return cache.get(gaussKey(sigma), interval);
	}

	public void prefetchDerivative(double sigma, int dimension, Interval interval) {
		cache.request(new DerivativeContent(cache, gaussKey(sigma), dimension), interval);
	}

	public GpuView derivative(double sigma, int dimension, Interval interval) {
		return cache.get(new DerivativeContent(cache, gaussKey(sigma), dimension), interval);
	}

	public void prefetchSecondDerivative(double sigma, int dimensionA, int dimensionB,
		Interval interval)
	{
		cache.request(secondDerivativeKey(sigma, dimensionA, dimensionB), interval);
	}

	public GpuView secondDerivative(double sigma, int dimensionA, int dimensionB, Interval interval) {
		return cache.get(secondDerivativeKey(sigma, dimensionA, dimensionB), interval);
	}

	private GaussContent gaussKey(double sigma) {
		return new GaussContent(cache, sigma);
	}

	private ComputeCache.Content secondDerivativeKey(double sigma, int dimensionA, int dimensionB) {
		if (dimensionA > dimensionB)
			return secondDerivativeKey(sigma, dimensionB, dimensionA);
		if (dimensionA == dimensionB)
			return new SecondDerivativeContent(cache, gaussKey(sigma), dimensionA);
		return new DerivativeContent(cache, new DerivativeContent(cache, gaussKey(sigma), dimensionA),
			dimensionB);
	}

	@Override
	public void close() {
		cache.close();
	}

	public Interval targetInterval() {
		return targetInterval;
	}

	public GpuApi gpuApi() {
		return gpu;
	}
}
