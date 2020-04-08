
package net.imglib2.trainable_segmention.gpu.compute_cache;

import net.imglib2.trainable_segmention.gpu.api.CLIJLoopBuilder;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
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
	public GpuImage load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		double[] pixelSize = cache.pixelSize();
		GpuView source = cache.get(input, requiredInput(interval));
		FinalInterval centerInterval = expand(new FinalInterval(source.dimensions()), -1, d);
		GpuView center = GpuViews.crop(source, centerInterval);
		GpuView front = GpuViews.crop(source, Intervals.translate(centerInterval, 1, d));
		GpuView back = GpuViews.crop(source, Intervals.translate(centerInterval, -1, d));
		GpuImage result = gpu.create(Intervals.dimensionsAsLongArray(interval), NativeTypeEnum.Float);
		CLIJLoopBuilder.gpu(gpu)
			.addInput("f", front)
			.addInput("b", back)
			.addInput("c", center)
			.addOutput("r", result)
			.addInput("factor", (float) (1 / Math.pow(pixelSize[d], 2)))
			.forEachPixel("r = (f + b - 2 * c) * factor");
		return result;
	}
}
