
package net.imglib2.trainable_segmentation.pixel_feature.filter.gauss;

import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "gaussian blur (group)")
public class GaussianBlurFeature extends AbstractSigmaGroupFeatureOp {

	public GaussianBlurFeature() {
		super(false);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGaussianBlurFeature.class;
	}
}
