
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.stats;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractGroupFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Min/Max/Mean/Median/Variance (for each sigma)")
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
			.map(r -> new FeatureSetting(SingleSphereShapedFeature.class, "radius", r, "operation",
				operation))
			.collect(Collectors.toList());
	}
}
