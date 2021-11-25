
package net.imglib2.trainable_segmentation.gpu.compute_cache;

import net.imglib2.trainable_segmentation.gpu.algorithms.GpuGauss;
import net.imglib2.trainable_segmentation.gpu.algorithms.GpuNeighborhoodOperation;
import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.imglib2.trainable_segmentation.gpu.api.GpuApi;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.Interval;
import net.imglib2.trainable_segmentation.gpu.api.GpuView;
import net.imglib2.trainable_segmentation.gpu.api.GpuViews;
import net.imglib2.util.Intervals;

import java.util.stream.DoubleStream;

public class GpuGaussContent implements GpuComputeCache.Content {

	private final GpuComputeCache cache;

	private final double sigma;

	private final GpuOriginalContent source;

	private final GpuNeighborhoodOperation operation;

	public GpuGaussContent(GpuComputeCache cache, double sigma) {
		this.cache = cache;
		this.sigma = sigma;
		this.source = new GpuOriginalContent(cache);
		this.operation = GpuGauss.gauss(cache.gpuApi(), sigmas(cache.pixelSize()));
	}

	@Override
	public int hashCode() {
		return Double.hashCode(sigma);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GpuGaussContent &&
			source.equals(((GpuGaussContent) obj).source) &&
			sigma == ((GpuGaussContent) obj).sigma;
	}

	@Override
	public void request(Interval interval) {
		cache.request(source, operation.getRequiredInputInterval(interval));
	}

	@Override
	public GpuImage load(Interval interval) {
		GpuApi gpu = cache.gpuApi();
		GpuView original = cache.get(source, operation.getRequiredInputInterval(interval));
		GpuImage output = gpu.create(Intervals.dimensionsAsLongArray(interval), NativeTypeEnum.Float);
		operation.apply(original, GpuViews.wrap(output));
		return output;
	}

	private double[] sigmas(double[] pixelSize) {
		return DoubleStream.of(pixelSize).map(p -> sigma / p).toArray();
	}
}
