
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Hessian (Group)")
public class HessianFeature extends AbstractSigmaGroupFeatureOp {

	public HessianFeature() {
		super(true);
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleHessianFeature.class;
	}
}
