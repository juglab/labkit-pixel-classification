/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.gpu.compute_cache;

import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
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
