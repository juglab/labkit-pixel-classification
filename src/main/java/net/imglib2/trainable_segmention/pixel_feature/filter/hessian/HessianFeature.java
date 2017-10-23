package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Hessian Feature 2D")
public class HessianFeature extends AbstractSigmaGroupFeatureOp {

	public HessianFeature() {
		super(true);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleHessianFeature.class;
	}
}
