package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.SingleDifferenceOfGaussiansFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mattias Arzt
 */
public class DifferenceOfGaussiansFeature {

	private DifferenceOfGaussiansFeature() {
		// prevent from being instantiated
	}

	public static Feature group() {
		return new FeatureGroup(initFeatures());
	}

	private static List<Feature> initFeatures() {
		List<Feature> features = new ArrayList<>();
		final double minimumSigma = 1;
		final double maximumSigma = 16;
		for (double sigma1 = minimumSigma; sigma1 <= maximumSigma; sigma1 *= 2)
			for (double sigma2 = minimumSigma; sigma2 < sigma1; sigma2 *= 2)
				features.add(new SingleDifferenceOfGaussiansFeature(sigma1, sigma2));
		return features;
	}
}
