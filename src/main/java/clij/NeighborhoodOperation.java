
package clij;

import net.imglib2.Interval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;

public interface NeighborhoodOperation {

	Interval getRequiredInputInterval(Interval targetInterval);

	void convolve(CLIJView input, CLIJView output);
}
