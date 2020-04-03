
package net.imglib2.trainable_segmention.clij_random_forest;

import clij.GpuImage;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import java.util.stream.LongStream;

public class CLIJView implements AutoCloseable {

	private final GpuImage buffer;
	private final Interval interval;

	private CLIJView(GpuImage buffer, Interval interval) {
		this.buffer = buffer;
		this.interval = interval;
	}

	public static CLIJView interval(GpuImage buffer, Interval interval) {
		return new CLIJView(buffer, interval);
	}

	public GpuImage buffer() {
		return buffer;
	}

	public Interval interval() {
		return interval;
	}

	public static CLIJView shrink(GpuImage buffer, long border) {
		return interval(buffer, Intervals.expand(new FinalInterval(buffer.getDimensions()), -border));
	}

	public static CLIJView shrink(GpuImage buffer, long[] border) {
		long[] negativeBorder = LongStream.of(border).map(x -> -x).toArray();
		return interval(buffer, Intervals.expand(new FinalInterval(buffer.getDimensions()),
			negativeBorder));
	}

	public static CLIJView wrap(GpuImage buffer) {
		return interval(buffer, new FinalInterval(buffer.getDimensions()));
	}

	@Override
	public void close() {
		buffer.close();
	}
}
