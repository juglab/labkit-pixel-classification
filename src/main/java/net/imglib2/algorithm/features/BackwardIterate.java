package net.imglib2.algorithm.features;

import net.imglib2.*;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@link BackwardIterate} helps to Iterate over Interval in opposite
 * order then {@link net.imglib2.view.Views#flatIterable(RandomAccessibleInterval)}.
 *
 * @author Matthias Arzt
 */
class BackwardIterate implements Iterator<Localizable> {

	private final long[] min;

	private final long[] max;

	private final long[] position;

	private final Localizable localizable;

	private long size;

	private BackwardIterate(Interval interval) {
		min = new long[interval.numDimensions()];
		interval.min(min);
		max = new long[interval.numDimensions()];
		interval.max(max);
		position = max.clone();
		position[0]++;
		localizable = Point.wrap(position);
		size = sizeOfInerval(interval);
	}

	private static long sizeOfInerval(Interval interval) {
		return IntStream.range(0, interval.numDimensions()).mapToLong(i ->
			interval.max(i) - interval.min(i) + 1).reduce(1, (a,b) -> a * b);
	}

	public static Iterable<Localizable> iterable(Interval interval) {
		return () -> new BackwardIterate(interval);
	}

	@Override
	public boolean hasNext() {
		return size > 0;
	}

	@Override
	public Localizable next() {
		size--;
		next(0);
		return localizable;
	}

	private void next(int d) {
		if (position[d] == min[d]) {
			position[d] = max[d];
			if (d + 1 < min.length)
				next(d + 1);
		} else position[d]--;
	}
}
