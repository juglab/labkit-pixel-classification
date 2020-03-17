package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import java.util.stream.LongStream;

public class CLIJView implements AutoCloseable {

	private final ClearCLBuffer buffer;
	private final Interval interval;

	private CLIJView(ClearCLBuffer buffer, Interval interval) {
		this.buffer = buffer;
		this.interval = interval;
	}

	public static CLIJView interval(ClearCLBuffer buffer, Interval interval) {
		return new CLIJView(buffer, interval);
	}

	public ClearCLBuffer buffer() {
		return buffer;
	}

	public Interval interval() {
		return interval;
	}

	public static CLIJView shrink(ClearCLBuffer buffer, long border) {
		return interval(buffer, Intervals.expand(new FinalInterval(buffer.getDimensions()), -border) );
	}

	public static CLIJView shrink(ClearCLBuffer buffer, long[] border) {
		long[] negativeBorder = LongStream.of(border).map(x -> -x).toArray();
		return interval(buffer, Intervals.expand(new FinalInterval(buffer.getDimensions()), negativeBorder));
	}

	public static CLIJView wrap(ClearCLBuffer buffer) {
		return interval(buffer, new FinalInterval(buffer.getDimensions()));
	}

	@Override
	public void close() {
		buffer.close();
	}
}
