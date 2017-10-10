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
@Plugin(type = FeatureOp.class, label = "Hessian Feature 3D")
public class Hessian3DFeature extends AbstractGroupFeatureOp {

	@Parameter
	private boolean absoluteValues = true;

	@Override
	protected List<FeatureSetting> initFeatures() {
		return globalSettings().sigmas().stream()
				.map(sigma -> SingleFeatures.hessian3d(sigma, absoluteValues))
				.collect(Collectors.toList());
	}
}
