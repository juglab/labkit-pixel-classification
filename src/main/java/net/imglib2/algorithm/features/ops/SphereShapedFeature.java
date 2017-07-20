package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.Features;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
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

	private static final List<Double> RADIUS = Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0);

	protected List<Feature> initFeatures() {
		return RADIUS.stream()
				.map(r -> Features.create(SingleSphereShapedFeature.class, r, operation))
				.collect(Collectors.toList());
	}
}
