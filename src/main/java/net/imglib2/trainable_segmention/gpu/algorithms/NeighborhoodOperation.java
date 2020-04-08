
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.imglib2.Interval;
import net.imglib2.trainable_segmention.gpu.api.GpuView;

public interface NeighborhoodOperation {

	Interval getRequiredInputInterval(Interval targetInterval);

	void convolve(GpuView input, GpuView output);
}
