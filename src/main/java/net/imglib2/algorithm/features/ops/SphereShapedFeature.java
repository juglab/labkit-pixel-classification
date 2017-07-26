package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.GlobalSettings;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
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

	private List<Double> initSigmas() {
		GlobalSettings settings = globalSettings();
		List<Double> sigmas = new ArrayList<>();
		for(double sigma = settings.minSigma(), maxSigma = settings.maxSigma(); sigma <= maxSigma; sigma *= 2.0)
			sigmas.add(sigma);
		return sigmas;
	}

	protected List<FeatureOp> initFeatures() {
		return initSigmas().stream()
				.map(r -> Features.create(SingleSphereShapedFeature.class, globalSettings(), r, operation))
				.collect(Collectors.toList());
	}
}
