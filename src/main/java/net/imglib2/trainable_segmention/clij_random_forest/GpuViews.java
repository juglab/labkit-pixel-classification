
package net.imglib2.trainable_segmention.clij_random_forest;

import clij.GpuApi;
import clij.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;

import java.util.stream.LongStream;

public class GpuViews {

	public static GpuView wrap(GpuImage buffer) {
		return new GpuView(buffer, new FinalDimensions(buffer.getDimensions()), 0);
	}

	public static GpuView crop(GpuImage image, Interval interval) {
		long[] dimensions = image.getDimensions();
		long offset = IntervalIndexer.positionToIndex(Intervals.minAsLongArray(interval), dimensions);
		return new GpuView(image, new FinalDimensions(interval), offset);
	}

	public static GpuView crop(GpuView image, Interval interval) {
		GpuImage source = image.source();
		long[] dimensions = source.getDimensions();
		long offset = IntervalIndexer.positionToIndex(Intervals.minAsLongArray(interval), dimensions);
		return new GpuView(source, new FinalDimensions(interval), offset + image.offset());
	}

	public static GpuView shrink(GpuImage image, long[] border) {
		long[] negativeBorder = LongStream.of(border).map(x -> -x).toArray();
		return crop(image, Intervals.expand(new FinalInterval(image.getDimensions()), negativeBorder));
	}

	public static GpuImage asGpuImage(GpuApi gpu, GpuView view) {
		GpuImage buffer = gpu.create(Intervals.dimensionsAsLongArray(view.dimensions()),
			NativeTypeEnum.Float);
		CLIJCopy.copy(gpu, view, wrap(buffer));
		return buffer;
	}
}
