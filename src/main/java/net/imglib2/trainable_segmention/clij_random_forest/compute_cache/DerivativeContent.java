
package net.imglib2.trainable_segmention.clij_random_forest.compute_cache;

import clij.CLIJLoopBuilder;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import clij.GpuApi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.util.Intervals;

import java.util.Objects;

public class DerivativeContent implements ComputeCache.Content {

	private final ComputeCache cache;
	private final ComputeCache.Content input;
	private final int d;

	public DerivativeContent(ComputeCache cache, ComputeCache.Content input, int d) {
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
		return obj instanceof DerivativeContent &&
			input.equals(((DerivativeContent) obj).input) &&
			d == ((DerivativeContent) obj).d;
	}

	@Override
	public void request(Interval interval) {
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
	public ClearCLBuffer load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		double[] pixelSize = cache.pixelSize();
		CLIJView source = cache.get(input, requiredInput(interval));
		Interval center = shrink(source.interval());
		CLIJView front = CLIJView.interval(source.buffer(), Intervals.translate(center, 1, d));
		CLIJView back = CLIJView.interval(source.buffer(), Intervals.translate(center, -1, d));
		ClearCLBuffer result = gpu.create(Intervals.dimensionsAsLongArray(center),
			NativeTypeEnum.Float);
		CLIJLoopBuilder.gpu(gpu)
			.addInput("f", front)
			.addInput("b", back)
			.addInput("factor", 0.5 / pixelSize[d])
			.addOutput("r", result)
			.forEachPixel("r = (f - b) * factor");
		return result;
	}
}
