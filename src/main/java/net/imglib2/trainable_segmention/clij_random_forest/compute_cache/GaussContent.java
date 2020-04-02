
package net.imglib2.trainable_segmention.clij_random_forest.compute_cache;

import clij.Gauss;
import clij.NeighborhoodOperation;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.util.Intervals;

import java.util.stream.DoubleStream;

public class GaussContent implements ComputeCache.Content {

	private final ComputeCache cache;

	private final double sigma;

	private final OriginalContent source;

	private final NeighborhoodOperation operation;

	public GaussContent(ComputeCache cache, double sigma) {
		this.cache = cache;
		this.sigma = sigma;
		this.source = new OriginalContent(cache);
		this.operation = Gauss.gauss(cache.clij(), sigmas(cache.pixelSize()));
	}

	@Override
	public int hashCode() {
		return Double.hashCode(sigma);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GaussContent &&
			source.equals(((GaussContent) obj).source) &&
			sigma == ((GaussContent) obj).sigma;
	}

	@Override
	public void request(Interval interval) {
		cache.request(source, operation.getRequiredInputInterval(interval));
	}

	@Override
	public ClearCLBuffer load(Interval interval) {
		CLIJ2 clij = cache.clij();
		CLIJView original = cache.get(source, operation.getRequiredInputInterval(interval));
		ClearCLBuffer output = clij.create(Intervals.dimensionsAsLongArray(interval));
		operation.convolve(original, CLIJView.wrap(output));
		return output;
	}

	private double[] sigmas(double[] pixelSize) {
		return DoubleStream.of(pixelSize).map(p -> sigma / p).toArray();
	}
}
