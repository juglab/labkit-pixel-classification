package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.GaussFeatureOp;

/**
 * @author Matthias Arzt
 */
public class GaussFeature extends FeatureGroup {

	private GaussFeature() {
		// prevent from being instantiated
	}

	public static Feature group() {
		return Features.create(net.imglib2.algorithm.features.ops.GaussFeature.class);
	}

	public static Feature single(double sigma) {
		return Features.create(GaussFeatureOp.class, sigma);
	}
}
