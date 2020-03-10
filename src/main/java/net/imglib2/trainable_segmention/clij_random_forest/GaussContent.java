
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import java.util.stream.DoubleStream;

public class GaussContent implements Request.Content {

	private final CLIJ2 clij;
	private final double[] pixelSize;
	private final double sigma;
	private final Request original;

	public GaussContent(CLIJ2 clij, Request original, double[] pixelSize, double sigma) {
		this.clij = clij;
		this.pixelSize = pixelSize;
		this.sigma = sigma;
		this.original = original;
	}

	@Override
	public void request(Interval interval) {
		original.request(requiredInput(interval));
	}

	@Override
	public ClearCLBuffer load(Interval interval) {
		CLIJView original = this.original.get(requiredInput(interval));
		try (
			ClearCLBuffer inputCL = copyView(clij, original);
			ClearCLBuffer tmp = clij.create(inputCL);)
		{
			double[] sigmas = sigmas();
			if (sigmas.length == 2)
				clij.gaussianBlur2D(inputCL, tmp, sigmas[0], sigmas[1]);
			else
				clij.gaussianBlur3D(inputCL, tmp, sigmas[0], sigmas[1], sigmas[2]);
			ClearCLBuffer output = clij.create(Intervals.dimensionsAsLongArray(interval),
				NativeTypeEnum.Float);
			CLIJCopy.copy(clij, CLIJView.shrink(tmp, borders()), CLIJView.wrap(output));
			return output;
		}
	}

	private Interval requiredInput(Interval interval) {
		return Intervals.expand(interval, borders());
	}

	private long[] borders() {
		return DoubleStream.of(sigmas()).mapToLong(x -> (long) (4 * x)).toArray();
	}

	private double[] sigmas() {
		return DoubleStream.of(pixelSize).map(p -> sigma / p).toArray();
	}

	private ClearCLBuffer copyView(CLIJ2 clij, CLIJView inputClBuffer) {
		ClearCLBuffer buffer = clij.create(Intervals.dimensionsAsLongArray(inputClBuffer.interval()));
		CLIJCopy.copy(clij, inputClBuffer, CLIJView.wrap(buffer));
		return buffer;
	}
}
