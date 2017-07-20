package net.imglib2.algorithm.features;

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

	public static Feature sobelSignle(double sigma) {
		return Features.create(SingleSobelGradientFeature.class, sigma);
	}

	public static Feature sobelGroup() {
		return Features.create(SobelGradientFeature.class);
	}

	public static Feature single(double sigma) {
		return Features.create(SingleGradientFeature.class, sigma);
	}

	public static Feature group() {
		return Features.create(net.imglib2.algorithm.features.ops.GradientFeature.class);
	}
}
