
package net.imglib2.trainable_segmentation.gpu.compute_cache;

import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.imglib2.trainable_segmentation.gpu.api.GpuApi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmentation.gpu.api.GpuView;
import net.imglib2.trainable_segmentation.gpu.api.GpuViews;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class GpuComputeCache {

	private final Map<Content, CacheEntry> map = new HashMap<>();
	private final GpuApi gpu;
	private final RandomAccessible<FloatType> original;
	private final double[] pixelSize;

	public GpuComputeCache(GpuApi gpu, RandomAccessible<FloatType> original, double[] pixelSize) {
		this.gpu = gpu;
		this.original = original;
		this.pixelSize = pixelSize;
	}

	public void request(Content content, Interval interval) {
		CacheEntry cacheEntry = map.computeIfAbsent(content, key -> new CacheEntry(content));
		cacheEntry.request(interval);
	}

	public GpuApi gpuApi() {
		return gpu;
	}

	public RandomAccessible<FloatType> original() {
		return original;
	}

	public double[] pixelSize() {
		return pixelSize;
	}

	public GpuView get(Content content, Interval interval) {
		CacheEntry cacheEntry = map.get(content);
		if (cacheEntry == null)
			throw new NoSuchElementException("Content was never requested: " + content);
		return cacheEntry.get(interval);
	}

	public interface Content {

		void request(Interval interval);

		GpuImage load(Interval interval);
	}

	class CacheEntry {

		private final Content content;
		private Interval requestedInterval = null;
		private GpuImage buffer = null;

		public CacheEntry(Content content) {
			this.content = content;
		}

		public void request(Interval interval) {
			if (buffer != null)
				throw new IllegalStateException("Image was already used, prefetch isn't allowed anymore.");
			if (requestedInterval == null) {
				requestedInterval = interval;
			}
			else {
				if (Intervals.contains(this.requestedInterval, interval))
					return;
				requestedInterval = Intervals.union(requestedInterval, interval);
			}
			content.request(requestedInterval);
		}

		public GpuView get(Interval interval) {
			if (requestedInterval == null || !Intervals.contains(this.requestedInterval, interval))
				throw new AssertionError("Interval was not prefetched.");
			if (buffer == null) {
				buffer = content.load(this.requestedInterval);
			}
			FinalInterval roi = Intervals.translateInverse(interval, Intervals.minAsLongArray(
				this.requestedInterval));
			return GpuViews.crop(buffer, roi);
		}
	}
}
