package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class OriginalContent implements Request.Content {

	private final CLIJ2 clij;
	private final RandomAccessible<FloatType> original;

	public OriginalContent(CLIJ2 clij, RandomAccessible<FloatType> original) {
		this.clij = clij;
		this.original = original;
	}

	@Override
	public void request(Interval interval) {

	}

	@Override
	public ClearCLBuffer load(Interval interval) {
		return clij.push(Views.interval(original, interval));
	}
}
