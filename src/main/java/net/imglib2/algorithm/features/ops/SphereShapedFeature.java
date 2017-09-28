package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.FeatureSetting;
import net.imglib2.algorithm.features.SingleFeatures;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class SphereShapedFeature extends AbstractGroupFeatureOp {

	@Parameter(choices = {
			SingleSphereShapedFeature.MAX,
			SingleSphereShapedFeature.MIN,
			SingleSphereShapedFeature.MEAN,
			SingleSphereShapedFeature.MEDIAN,
			SingleSphereShapedFeature.VARIANCE
	})
	private String operation;

	protected List<FeatureSetting> initFeatures() {
		return globalSettings().sigmas().stream()
				.map(r -> SingleFeatures.sphereOperation(r, operation))
				.collect(Collectors.toList());
	}
}
