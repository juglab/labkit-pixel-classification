
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.trainable_segmention.utils.AutoClose;
import net.imglib2.util.Intervals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class GpuConcatenatedNeighborhoodOperation implements GpuNeighborhoodOperation {

	private final GpuApi gpu;

	private final List<? extends GpuNeighborhoodOperation> convolutions;

	public GpuConcatenatedNeighborhoodOperation(GpuApi gpu,
		List<? extends GpuNeighborhoodOperation> convolutions)
	{
		this.gpu = gpu;
		this.convolutions = convolutions;
	}

	@Override
	public Interval getRequiredInputInterval(Interval targetInterval) {
		return intervals(targetInterval).get(0);
	}

	@Override
	public void apply(GpuView input, GpuView output) {
		try (AutoClose autoClose = new AutoClose()) {
			List<Interval> intervals = intervals(new FinalInterval(output.dimensions()));
			if (!Intervals.equalDimensions(new FinalInterval(input.dimensions()), intervals.get(0)))
				throw new IllegalArgumentException("Dimensions of the input image are not as expected.");
			int n = convolutions.size();
			List<GpuView> buffers = new ArrayList<>(n);
			buffers.add(input);
			for (int i = 1; i < n; i++) {
				long[] dimensions = Intervals.dimensionsAsLongArray(intervals.get(i));
				GpuImage buffer = gpu.create(dimensions, NativeTypeEnum.Float);
				buffers.add(GpuViews.wrap(buffer));
				autoClose.add(buffer);
			}
			buffers.add(output);
			for (int i = 0; i < convolutions.size(); i++) {
				GpuNeighborhoodOperation convolution = convolutions.get(i);
				convolution.apply(buffers.get(i), buffers.get(i + 1));
			}
		}
	}

	private List<Interval> intervals(Interval outputInterval) {
		int n = convolutions.size();
		List<Interval> intervals = new ArrayList<>(n + 1);
		intervals.add(outputInterval);
		Interval t = outputInterval;
		for (int i = n - 1; i >= 0; i--) {
			GpuNeighborhoodOperation convolution = convolutions.get(i);
			t = convolution.getRequiredInputInterval(t);
			intervals.add(t);
		}
		Collections.reverse(intervals);
		return intervals;
	}
}
