
package net.imglib2.trainable_segmention.clij_random_forest.compute_cache;

import clij.CLIJLoopBuilder;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.util.Intervals;

import java.util.Objects;

public class DerivativeContent implements ComputeCache.Content {

	private final ComputeCache.Content input;
	private final int d;

	public DerivativeContent(ComputeCache.Content input, int d) {
		this.input = input;
		this.d = d;
	}

	@Override
	public int hashCode() {
		return Objects.hash(input, d);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DerivativeContent &&
			input.equals(((DerivativeContent) obj).input) &&
			d == ((DerivativeContent) obj).d;
	}

	@Override
	public void request(ComputeCache cache, Interval interval) {
		cache.request(input, requiredInput(interval));
	}

	private FinalInterval requiredInput(Interval interval) {
		long[] border = new long[interval.numDimensions()];
		border[d] = 1;
		return Intervals.expand(interval, border);
	}

	private FinalInterval shrink(Interval interval) {
		long[] border = new long[interval.numDimensions()];
		border[d] = -1;
		return Intervals.expand(interval, border);
	}

	@Override
	public ClearCLBuffer load(ComputeCache cache, Interval interval) {
		CLIJ2 clij = cache.clij();
		double[] pixelSize = cache.pixelSize();
		CLIJView source = cache.get(input, requiredInput(interval));
		Interval center = shrink(source.interval());
		CLIJView front = CLIJView.interval(source.buffer(), Intervals.translate(center, 1, d));
		CLIJView back = CLIJView.interval(source.buffer(), Intervals.translate(center, -1, d));
		ClearCLBuffer result = clij.create(Intervals.dimensionsAsLongArray(center),
			NativeTypeEnum.Float);
		CLIJLoopBuilder.clij(clij)
			.addInput("f", front)
			.addInput("b", back)
			.addInput("factor", 0.5 / pixelSize[d])
			.addOutput("r", result)
			.forEachPixel("r = (f - b) * factor");
		return result;
	}
}
