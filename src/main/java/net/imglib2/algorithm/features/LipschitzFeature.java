package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.SingleLipschitzFeature;

/**
 * @author Matthias Arzt
 */
public class LipschitzFeature {

	private LipschitzFeature() {
		// prevent from being instantiated
	}

	public static Feature group(long border) {
		return Features.create(net.imglib2.algorithm.features.ops.LipschitzFeature.class, border);
	}

	public static Feature single(double slope, long border) {
		return Features.create(SingleLipschitzFeature.class, slope, border);
	}

}
