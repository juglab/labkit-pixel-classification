
package net.imglib2.trainable_segmentation.pixel_feature.filter.hessian;

import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;
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
