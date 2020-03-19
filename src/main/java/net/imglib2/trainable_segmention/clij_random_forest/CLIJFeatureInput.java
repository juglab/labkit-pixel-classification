package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.ComputeCache;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.DerivativeContent;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.GaussContent;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.OriginalContent;
import net.imglib2.trainable_segmention.clij_random_forest.compute_cache.SecondDerivativeContent;
import net.imglib2.type.numeric.real.FloatType;

public class CLIJFeatureInput implements AutoCloseable {

	private final CLIJ2 clij;
	private final Interval targetInterval;

	private final ComputeCache cache;

	public CLIJFeatureInput(CLIJ2 clij, RandomAccessible<FloatType> original, Interval targetInterval, double[] pixelSize) {
		this.clij = clij;
		this.targetInterval = targetInterval;
		this.cache = new ComputeCache(clij, original, pixelSize);
	}

	public void prefetchOriginal(Interval interval) {
		cache.request(new OriginalContent(), interval);
	}

	public CLIJView original(Interval interval) {
		return cache.get(new OriginalContent(), interval);
	}

	public void prefetchGauss(double sigma, Interval interval) {
		cache.request(gaussKey(sigma), interval);
	}

	public CLIJView gauss(double sigma, Interval interval) {
		return cache.get(gaussKey(sigma), interval);
	}

	public void prefetchDerivative(double sigma, int dimension, Interval interval) {
		cache.request(new DerivativeContent(gaussKey(sigma), dimension), interval);
	}

	public CLIJView derivative(double sigma, int dimension, Interval interval) {
		return cache.get(new DerivativeContent(gaussKey(sigma), dimension), interval);
	}

	public void prefetchSecondDerivative(double sigma, int dimensionA, int dimensionB, Interval interval) {
		cache.request(secondDerivativeKey(sigma, dimensionA, dimensionB), interval);
	}

	public CLIJView secondDerivative(double sigma, int dimensionA, int dimensionB, Interval interval) {
		return cache.get(secondDerivativeKey(sigma, dimensionA, dimensionB), interval);
	}

	private GaussContent gaussKey(double sigma) {
		return new GaussContent(sigma);
	}

	private ComputeCache.Content secondDerivativeKey(double sigma, int dimensionA, int dimensionB) {
		if(dimensionA > dimensionB)
			return secondDerivativeKey(sigma, dimensionB, dimensionA);
		if(dimensionA == dimensionB)
			return new SecondDerivativeContent(gaussKey(sigma), dimensionA);
		return new DerivativeContent(new DerivativeContent(gaussKey(sigma), dimensionA), dimensionB);
	}

	@Override
	public void close() {
		cache.close();
	}

	public Interval targetInterval() {
		return targetInterval;
	}

	public CLIJ2 clij() {
		return clij;
	}
}
