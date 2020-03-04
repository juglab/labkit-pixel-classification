
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.utils.AutoClose;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class CLIJFeatureInput implements AutoCloseable {

	private final CLIJ2 clij;
	private final AutoClose autoClose = new AutoClose();
	private final RandomAccessible<FloatType> original;
	private final Interval targetInterval;
	private final double[] pixelSize;

	private Interval requestedOriginalInterval;
	private ClearCLBuffer originalBuffer = null;

	public CLIJFeatureInput(CLIJ2 clij, RandomAccessible<FloatType> original, Interval targetInterval,
		double[] pixelSize)
	{
		this.clij = clij;
		this.original = original;
		this.targetInterval = targetInterval;
		this.pixelSize = pixelSize;
		this.requestedOriginalInterval = targetInterval;
	}

	public void prefetchOriginal(FinalInterval interval) {
		if (originalBuffer != null)
			throw new IllegalStateException("Image was already used, prefetch isn't allowed anymore.");
		requestedOriginalInterval = Intervals.union(requestedOriginalInterval, interval);
	}

	/**
	 * Returns a copy of the given interval of the original image. The buffer of the
	 * CLIJView returned might be bigger that the requested interval. Only the roi
	 * of the CLIJView has to be taken into account.
	 */
	public CLIJView original(FinalInterval interval) {
		if (!Intervals.contains(requestedOriginalInterval, interval))
			throw new AssertionError("Interval was not prefetched.");
		if (originalBuffer == null) {
			originalBuffer = clij.push(Views.interval(original, requestedOriginalInterval));
			autoClose.add(originalBuffer);
		}
		FinalInterval roi = Intervals.translateInverse(interval, Intervals.minAsLongArray(
			requestedOriginalInterval));
		return CLIJView.interval(originalBuffer, roi);
	}

	@Override
	public void close() {
		autoClose.close();
	}

	public Interval targetInterval() {
		return targetInterval;
	}

	public CLIJ2 clij() {
		return clij;
	}
}
