
package sc.fiji.labkit.pixel_classification.gpu.algorithms;

import net.imglib2.Interval;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;

public interface GpuNeighborhoodOperation {

	Interval getRequiredInputInterval(Interval targetInterval);

	void apply(GpuView input, GpuView output);
}
