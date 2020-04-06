
package clij;

import net.imglib2.Interval;
import net.imglib2.trainable_segmention.clij_random_forest.GpuView;

public interface NeighborhoodOperation {

	Interval getRequiredInputInterval(Interval targetInterval);

	void convolve(GpuView input, GpuView output);
}
