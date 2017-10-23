package net.imglib2.trainable_segmention.ops;

import net.imglib2.trainable_segmention.FeatureSetting;
import net.imglib2.trainable_segmention.SingleFeatures;
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
