package net.imglib2.trainable_segmention.ops;

import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
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
