package net.imglib2.trainable_segmention.ops;

import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class GradientFeature extends AbstractSigmaGroupFeatureOp {

	public GradientFeature() {
		super(true);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGradientFeature.class;
	}
}
