
package net.imglib2.trainable_segmention.clij_random_forest.compute_cache;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.utils.AutoClose;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class ComputeCache implements AutoCloseable {

	private final AutoClose autoClose = new AutoClose();
	private final Map<Content, CacheEntry> map = new HashMap<>();
	private final CLIJ2 clij;
	private final RandomAccessible<FloatType> original;
	private final double[] pixelSize;

	public ComputeCache(CLIJ2 clij, RandomAccessible<FloatType> original, double[] pixelSize) {
		this.clij = clij;
		this.original = original;
		this.pixelSize = pixelSize;
	}

	public void request(Content content, Interval interval) {
		CacheEntry cacheEntry = map.computeIfAbsent(content, key -> new CacheEntry(content));
		cacheEntry.request(interval);
	}

	public CLIJ2 clij() {
		return clij;
	}

	public RandomAccessible<FloatType> original() {
		return original;
	}

	public double[] pixelSize() {
		return pixelSize;
	}

	public CLIJView get(Content content, Interval interval) {
		CacheEntry cacheEntry = map.get(content);
		if (cacheEntry == null)
			throw new NoSuchElementException("Content was never requested: " + content);
		return cacheEntry.get(interval);
	}

	@Override
	public void close() {
		autoClose.close();
	}

	public interface Content {

		void request(Interval interval);

		ClearCLBuffer load(Interval interval);
	}

	class CacheEntry {

		private final Content content;
		private Interval requestedInterval = null;
		private ClearCLBuffer buffer = null;

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

		public CLIJView get(Interval interval) {
			if (requestedInterval == null || !Intervals.contains(this.requestedInterval, interval))
				throw new AssertionError("Interval was not prefetched.");
			if (buffer == null) {
				buffer = content.load(this.requestedInterval);
				autoClose.add(buffer);
			}
			FinalInterval roi = Intervals.translateInverse(interval, Intervals.minAsLongArray(
				this.requestedInterval));
			return CLIJView.interval(buffer, roi);
		}
	}
}
