package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.imglib2.FinalInterval.createMinSize;

public class CLIJFeatureStack implements AutoCloseable {

	private final CLIJ2 clij;
	private final Interval interval;
	private final int featureCount;
	private final ClearCLBuffer buffer;

	public CLIJFeatureStack(CLIJ2 clij, Interval interval, int featureCount) {
		this.clij = clij;
		this.interval = interval;
		this.featureCount = featureCount;
		long[] size = Intervals.dimensionsAsLongArray(interval);
		size[2] *= featureCount;
		this.buffer = clij.create(size);
	}

	public List<CLIJView> clijSlices() {
		return featureIntervals().stream().map(i -> CLIJView.interval(buffer, i)).collect(Collectors.toList());
	}

	public RandomAccessibleInterval<FloatType> asRAI() {
		RandomAccessibleInterval<FloatType> rai = clij.pullRAI(buffer);
		long[] offset = Intervals.minAsLongArray(interval);
		List<RandomAccessibleInterval<FloatType>> slices = featureIntervals().stream()
				.map(i -> Views.translate(Views.zeroMin(Views.interval(rai, i)), offset))
				.collect(Collectors.toList());
		return Views.stack(slices);
	}

	private List<Interval> featureIntervals() {
		long[] size = Intervals.dimensionsAsLongArray(interval);
		return IntStream.range(0, featureCount).mapToObj(i -> {
			long[] min = {0, 0, size[2] * i};
			return createMinSize(min, size);
		}).collect(Collectors.toList());
	}

	@Override
	public void close() {
		buffer.close();
	}

	public ClearCLBuffer asClearCLBuffer() {
		return buffer;
	}
}
