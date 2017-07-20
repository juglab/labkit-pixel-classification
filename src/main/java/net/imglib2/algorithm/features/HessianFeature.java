package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleHessianFeature;

/**
 * @author Matthias Arzt
 */
public class HessianFeature {

	private HessianFeature() {
		// prevent from being instantiated
	}

	public static FeatureOp single(double sigma) {
		return Features.create(SingleHessianFeature.class, sigma);
	}

	public static FeatureOp group() {
		return Features.create(net.imglib2.algorithm.features.ops.HessianFeature.class);
	}
}
