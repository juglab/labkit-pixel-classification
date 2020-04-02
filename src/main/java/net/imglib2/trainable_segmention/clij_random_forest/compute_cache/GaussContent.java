
package net.imglib2.trainable_segmention.clij_random_forest.compute_cache;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.util.Intervals;

import java.util.stream.DoubleStream;

public class GaussContent implements ComputeCache.Content {

	private final ComputeCache cache;

	private final double sigma;

	public GaussContent(ComputeCache cache, double sigma) {
		this.cache = cache;
		this.sigma = sigma;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(sigma);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GaussContent && ((GaussContent) obj).sigma == sigma;
	}

	@Override
	public void request(Interval interval) {
		Interval requiredInput = requiredInput(cache.pixelSize(), interval);
		cache.request(new OriginalContent(cache), requiredInput);
	}

	@Override
	public ClearCLBuffer load(Interval interval) {
		CLIJ2 clij = cache.clij();
		double[] pixelSize = cache.pixelSize();
		CLIJView original = cache.get(new OriginalContent(cache), requiredInput(pixelSize, interval));
		try (
			ClearCLBuffer inputCL = copyView(clij, original);
			ClearCLBuffer tmp = clij.create(inputCL);)
		{
			double[] sigmas = sigmas(pixelSize);
			if (sigmas.length == 2)
				clij.gaussianBlur2D(inputCL, tmp, sigmas[0], sigmas[1]);
			else
				clij.gaussianBlur3D(inputCL, tmp, sigmas[0], sigmas[1], sigmas[2]);
			ClearCLBuffer output = clij.create(Intervals.dimensionsAsLongArray(interval),
				NativeTypeEnum.Float);
			CLIJCopy.copy(clij, CLIJView.shrink(tmp, borders(pixelSize)), CLIJView.wrap(output));
			return output;
		}
	}

	private Interval requiredInput(double[] pixelSize, Interval interval) {
		return Intervals.expand(interval, borders(pixelSize));
	}

	private long[] borders(double[] pixelSize) {
		return DoubleStream.of(sigmas(pixelSize)).mapToLong(x -> (long) (4 * x)).toArray();
	}

	private double[] sigmas(double[] pixelSize) {
		return DoubleStream.of(pixelSize).map(p -> sigma / p).toArray();
	}

	private ClearCLBuffer copyView(CLIJ2 clij, CLIJView inputClBuffer) {
		ClearCLBuffer buffer = clij.create(Intervals.dimensionsAsLongArray(inputClBuffer.interval()));
		CLIJCopy.copy(clij, inputClBuffer, CLIJView.wrap(buffer));
		return buffer;
	}
}
