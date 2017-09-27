package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.Features;
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

	protected List<FeatureOp> initFeatures() {
		return globalSettings().sigmas().stream()
				.map(r -> Features.create(ops(), SingleSphereShapedFeature.class, globalSettings(), r, operation))
				.collect(Collectors.toList());
	}
}
