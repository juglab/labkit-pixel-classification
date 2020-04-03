
package net.imglib2.trainable_segmention.clij_random_forest.compute_cache;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import clij.GpuApi;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.StopWatch;
import net.imglib2.view.Views;

public class OriginalContent implements ComputeCache.Content {

	private final ComputeCache cache;

	public OriginalContent(ComputeCache cache) {
		this.cache = cache;
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
	public void request(Interval interval) {

	}

	@Override
	public ClearCLBuffer load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		RandomAccessible<FloatType> original = cache.original();
		StopWatch watch = StopWatch.createAndStart();
		ClearCLBuffer push = gpu.push(Views.interval(original, interval));
		System.out.println("Time copying: " + watch);
		return push;
	}
}
