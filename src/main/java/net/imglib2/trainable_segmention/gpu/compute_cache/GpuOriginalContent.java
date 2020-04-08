
package net.imglib2.trainable_segmention.gpu.compute_cache;

import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.StopWatch;
import net.imglib2.view.Views;

public class GpuOriginalContent implements GpuComputeCache.Content {

	private final GpuComputeCache cache;

	public GpuOriginalContent(GpuComputeCache cache) {
		this.cache = cache;
	}

	@Override
	public int hashCode() {
		return 928374982;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GpuOriginalContent;
	}

	@Override
	public void request(Interval interval) {

	}

	@Override
	public GpuImage load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		RandomAccessible<FloatType> original = cache.original();
		StopWatch watch = StopWatch.createAndStart();
		GpuImage push = gpu.push(Views.interval(original, interval));
		System.out.println("Time copying: " + watch);
		return push;
	}
}
