
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.gauss;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "gaussian blur (for each sigma)")
public class GaussianBlurFeature extends AbstractSigmaGroupFeatureOp {

	public GaussianBlurFeature() {
		super(false);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGaussianBlurFeature.class;
	}
}
