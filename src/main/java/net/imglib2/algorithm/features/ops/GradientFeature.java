package net.imglib2.algorithm.features.ops;

import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class GradientFeature extends AbstractSigmaGroupFeatureOp {

	@Override
	protected double[] initSigmas() {
		return new double[]{0.0, 1.0, 2.0, 4.0, 8.0, 16.0};
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGradientFeature.class;
	}
}
