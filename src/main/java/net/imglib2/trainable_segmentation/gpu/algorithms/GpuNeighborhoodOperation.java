
package net.imglib2.trainable_segmentation.gpu.algorithms;

import net.imglib2.Interval;
import net.imglib2.trainable_segmentation.gpu.api.GpuView;

public interface GpuNeighborhoodOperation {

	Interval getRequiredInputInterval(Interval targetInterval);

	void apply(GpuView input, GpuView output);
}
