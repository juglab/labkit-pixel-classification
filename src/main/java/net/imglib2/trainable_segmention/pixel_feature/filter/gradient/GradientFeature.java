package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Gradient (Group)")
public class GradientFeature extends AbstractSigmaGroupFeatureOp {

	public GradientFeature() {
		super(false);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGradientFeature.class;
	}
}
