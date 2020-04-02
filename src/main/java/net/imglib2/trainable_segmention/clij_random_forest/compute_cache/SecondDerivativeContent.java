
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

public class SecondDerivativeContent implements ComputeCache.Content {

	private final ComputeCache cache;
	private final ComputeCache.Content input;
	private final int d;

	public SecondDerivativeContent(ComputeCache cache, ComputeCache.Content input, int d) {
		this.cache = cache;
		this.input = input;
		this.d = d;
	}

	@Override
	public int hashCode() {
		return Objects.hash(input, d);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SecondDerivativeContent &&
			input.equals(((SecondDerivativeContent) obj).input) &&
			d == ((SecondDerivativeContent) obj).d;
	}

	@Override
	public void request(Interval interval) {
		cache.request(input, requiredInput(interval));
	}

	private FinalInterval requiredInput(Interval interval) {
		return expand(interval, 1, d);
	}

	private static FinalInterval expand(Interval interval, int border, int d) {
		long[] b = new long[interval.numDimensions()];
		b[d] = border;
		return Intervals.expand(interval, b);
	}

	@Override
	public ClearCLBuffer load(Interval interval) {
		CLIJ2 clij = cache.clij();
		double[] pixelSize = cache.pixelSize();
		CLIJView source = cache.get(input, requiredInput(interval));
		CLIJView center = CLIJView.interval(source.buffer(), expand(source.interval(), -1, d));
		CLIJView front = CLIJView.interval(source.buffer(), Intervals.translate(center.interval(), 1,
			d));
		CLIJView back = CLIJView.interval(source.buffer(), Intervals.translate(center.interval(), -1,
			d));
		ClearCLBuffer result = clij.create(Intervals.dimensionsAsLongArray(center.interval()),
			NativeTypeEnum.Float);
		CLIJLoopBuilder.clij(clij)
			.addInput("f", front)
			.addInput("b", back)
			.addInput("c", center)
			.addOutput("r", result)
			.addInput("factor", (float) (1 / Math.pow(pixelSize[d], 2)))
			.forEachPixel("r = (f + b - 2 * c) * factor");
		return result;
	}
}
