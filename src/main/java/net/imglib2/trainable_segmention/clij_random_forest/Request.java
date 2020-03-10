package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

class Request implements AutoCloseable {
	private final Content content;
	private Interval interval;
	private ClearCLBuffer buffer = null;

	public Request(Interval interval, Content content)
	{
		this.content = content;
		this.interval = interval;
		content.request(interval);
	}

	public void request(Interval interval) {
		if(buffer != null)
			throw new IllegalStateException("Image was already used, prefetch isn't allowed anymore.");
		if(Intervals.contains(this.interval, interval))
			return;
		this.interval = Intervals.union(this.interval, interval);
		content.request(this.interval);
	}

	public CLIJView get(Interval interval) {
		if(!Intervals.contains(this.interval, interval))
			throw new AssertionError("Interval was not prefetched.");
		if(buffer == null) {
			buffer = content.load(this.interval);
		}
		FinalInterval roi = Intervals.translateInverse(interval, Intervals.minAsLongArray(this.interval));
		return CLIJView.interval(buffer, roi);
	}

	@Override
	public void close() {
		if (buffer != null) {
			buffer.close();
			buffer = null;
		}
	}

	public interface Content {

		void request(Interval interval);

		ClearCLBuffer load(Interval interval);
	}
}
