package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.SignleGradientFeature;
import net.imglib2.algorithm.features.ops.SobelGradientFeature;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class GradientFeature {

	private static final double[] SIGMAS = new double[]{0.0, 1.0, 2.0, 4.0, 8.0, 16.0};

	private GradientFeature() {
		// prevent from being instantiated
	}

	public static Feature sobelSignle(double sigma) {
		return new SobelGradientFeature(sigma);
	}

	public static Feature sobelGroup() {
		return new FeatureGroup(Arrays.stream(SIGMAS)
				.mapToObj(GradientFeature::sobelSignle)
				.collect(Collectors.toList()));
	}

	public static Feature single(double sigma) {
		return new SignleGradientFeature(sigma);
	}

	public static Feature group() {
		return new FeatureGroup(Arrays.stream(SIGMAS)
				.mapToObj(GradientFeature::single)
				.collect(Collectors.toList()));
	}
}
