package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.SingleHessianFeature;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class HessianFeature {

	private static final double[] SIGMAS = new double[]{0.0, 1.0, 2.0, 4.0, 8.0, 16.0};

	private HessianFeature() {
		// prevent from being instantiated
	}

	public static Feature group() {
		return new FeatureGroup(initFeatures());
	}

	private static List<Feature> initFeatures() {
		return Arrays.stream(SIGMAS)
				.mapToObj(SingleHessianFeature::new)
				.collect(Collectors.toList());
	}

}
