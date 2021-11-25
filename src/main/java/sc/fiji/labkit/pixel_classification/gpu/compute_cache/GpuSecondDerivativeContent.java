
package sc.fiji.labkit.pixel_classification.gpu.compute_cache;

import sc.fiji.labkit.pixel_classification.gpu.api.GpuPixelWiseOperation;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.util.Intervals;

import java.util.Objects;

public class GpuSecondDerivativeContent implements GpuComputeCache.Content {

	private final GpuComputeCache cache;
	private final GpuComputeCache.Content input;
	private final int d;

	public GpuSecondDerivativeContent(GpuComputeCache cache, GpuComputeCache.Content input, int d) {
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
		return obj instanceof GpuSecondDerivativeContent &&
			input.equals(((GpuSecondDerivativeContent) obj).input) &&
			d == ((GpuSecondDerivativeContent) obj).d;
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
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("f", front)
			.addInput("b", back)
			.addInput("c", center)
			.addOutput("r", result)
			.addInput("factor", (float) (1 / Math.pow(pixelSize[d], 2)))
			.forEachPixel("r = (f + b - 2 * c) * factor");
		return result;
	}
}
