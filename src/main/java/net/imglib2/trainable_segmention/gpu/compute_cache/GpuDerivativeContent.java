
package net.imglib2.trainable_segmention.gpu.compute_cache;

import net.imglib2.trainable_segmention.gpu.api.GpuPixelWiseOperation;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.util.Intervals;

import java.util.Objects;

public class GpuDerivativeContent implements GpuComputeCache.Content {

	private final GpuComputeCache cache;
	private final GpuComputeCache.Content input;
	private final int d;

	public GpuDerivativeContent(GpuComputeCache cache, GpuComputeCache.Content input, int d) {
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
		return obj instanceof GpuDerivativeContent &&
			input.equals(((GpuDerivativeContent) obj).input) &&
			d == ((GpuDerivativeContent) obj).d;
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
	public GpuImage load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		double[] pixelSize = cache.pixelSize();
		GpuView source = cache.get(input, requiredInput(interval));
		Interval center = shrink(new FinalInterval(source.dimensions()));
		GpuView front = GpuViews.crop(source, Intervals.translate(center, 1, d));
		GpuView back = GpuViews.crop(source, Intervals.translate(center, -1, d));
		GpuImage result = gpu.create(Intervals.dimensionsAsLongArray(center), NativeTypeEnum.Float);
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("f", front)
			.addInput("b", back)
			.addInput("factor", 0.5 / pixelSize[d])
			.addOutput("r", result)
			.forEachPixel("r = (f - b) * factor");
		return result;
	}
}
