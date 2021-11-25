
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.hessian;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "hessian eigenvalues (for each sigma)")
public class HessianEigenvaluesFeature extends AbstractSigmaGroupFeatureOp {

	public HessianEigenvaluesFeature() {
		super(true);
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2 || globals.numDimensions() == 3;
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleHessianEigenvaluesFeature.class;
	}
}
