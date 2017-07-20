package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleGradientFeature;
import net.imglib2.algorithm.features.ops.SingleSobelGradientFeature;
import net.imglib2.algorithm.features.ops.SobelGradientFeature;

/**
 * @author Matthias Arzt
 */
public class GradientFeature {

	private GradientFeature() {
		// prevent from being instantiated
	}

	public static FeatureOp sobelSignle(double sigma) {
		return Features.create(SingleSobelGradientFeature.class, sigma);
	}

	public static FeatureOp sobelGroup() {
		return Features.create(SobelGradientFeature.class);
	}

	public static FeatureOp single(double sigma) {
		return Features.create(SingleGradientFeature.class, sigma);
	}

	public static FeatureOp group() {
		return Features.create(net.imglib2.algorithm.features.ops.GradientFeature.class);
	}
}
