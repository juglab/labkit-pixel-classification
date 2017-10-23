package net.imglib2.trainable_segmention.ops;

import net.imglib2.trainable_segmention.FeatureSetting;
import net.imglib2.trainable_segmention.SingleFeatures;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class LipschitzFeature extends AbstractGroupFeatureOp {

	@Parameter
	private long border;

	protected List<FeatureSetting> initFeatures() {
		return Arrays.stream(new double[]{5, 10, 15, 20, 25})
				.mapToObj(slope -> SingleFeatures.lipschitz(slope, border))
				.collect(Collectors.toList());
	}
}
