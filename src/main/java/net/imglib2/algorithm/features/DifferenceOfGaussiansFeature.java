package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleDifferenceOfGaussiansFeature;

/**
 * @author Mattias Arzt
 */
public class DifferenceOfGaussiansFeature {

	private DifferenceOfGaussiansFeature() {
		// prevent from being instantiated
	}

	public static FeatureOp group() {
		return Features.create(net.imglib2.algorithm.features.ops.DifferenceOfGaussiansFeature.class);
	}

	public static FeatureOp single(double sigma1, double sigma2) {
		return Features.create(SingleDifferenceOfGaussiansFeature.class, sigma1, sigma2);
	}
}
