package net.imglib2.trainable_segmention.clij_random_forest.compute_cache;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class OriginalContent implements ComputeCache.Content {

	public OriginalContent() {
	}

	@Override
	public int hashCode() {
		return 928374982;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof OriginalContent;
	}

	@Override
	public void request(ComputeCache cache, Interval interval) {

	}

	@Override
	public ClearCLBuffer load(ComputeCache cache, Interval interval) {
		CLIJ2 clij = cache.clij();
		RandomAccessible<FloatType> original = cache.original();
		return clij.push(Views.interval(original, interval));
	}
}
