
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "gaussian gradient magnitude (for each sigma)")
public class GaussianGradientMagnitudeFeature extends AbstractSigmaGroupFeatureOp {

	public GaussianGradientMagnitudeFeature() {
		super(false);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGaussianGradientMagnitudeFeature.class;
	}
}
