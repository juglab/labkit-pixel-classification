package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.FeatureSetting;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.SingleFeatures;
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
