package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.utils.AutoClose;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

import java.util.HashMap;
import java.util.Map;

public class CLIJFeatureInput implements AutoCloseable {

	private final CLIJ2 clij;
	private final AutoClose autoClose = new AutoClose();
	private final RandomAccessible<FloatType> original;
	private final Interval targetInterval;
	private final double[] pixelSize;

	private final Request originalRequest;
	private final Map<Double, Request> gaussRequest;


	public CLIJFeatureInput(CLIJ2 clij, RandomAccessible<FloatType> original, Interval targetInterval, double[] pixelSize) {
		this.clij = clij;
		this.original = original;
		this.targetInterval = targetInterval;
		this.pixelSize = pixelSize;
		this.originalRequest = new Request(targetInterval, new OriginalContent(clij, original));
		this.gaussRequest = new HashMap<>();
	}

	private ClearCLBuffer copyView(CLIJ2 clij, CLIJView inputClBuffer) {
		ClearCLBuffer buffer = clij.create(Intervals.dimensionsAsLongArray(inputClBuffer.interval()));
		CLIJCopy.copy(clij, inputClBuffer, CLIJView.wrap(buffer));
		return buffer;
	}

	public void prefetchOriginal(Interval interval) {
		originalRequest.request(interval);
	}

	/**
	 * Returns a copy of the given interval of the original image.
	 * The buffer of the CLIJView returned might be bigger that the requested interval.
	 * Only the roi of the CLIJView has to be taken into account.
	 */
	public CLIJView original(FinalInterval interval) {
		return originalRequest.get(interval);
	}

	public void prefetchGauss(double sigma, Interval interval) {
		gaussRequest.computeIfAbsent(sigma, key -> new Request(targetInterval, new GaussContent(clij, originalRequest, pixelSize, sigma)));
		Request request = gaussRequest.get(sigma);
		request.request(interval);
	}

	public CLIJView gauss(double sigma, Interval interval) {
		return gaussRequest.get(sigma).get(interval);
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
