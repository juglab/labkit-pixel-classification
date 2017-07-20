package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.*;

/**
 * @author Matthias Arzt
 */
public class SingleFeatures {

	private SingleFeatures() {
		// prevent from being instantiated
	}

	public static FeatureOp identity() {
		return Features.create(IdendityFeature.class);
	}

	public static FeatureOp gabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		boolean legacyNormalize = false;
		return Features.create(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles, legacyNormalize);
	}

	public static FeatureOp legacyGabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		boolean legacyNormalize = true;
		return Features.create(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles, legacyNormalize);
	}

	public static FeatureOp gauss(double sigma) {
		return Features.create(SingleGaussFeature.class, sigma);
	}

	public static FeatureOp sobelGradient(double sigma) {
		return Features.create(SingleSobelGradientFeature.class, sigma);
	}

	public static FeatureOp gradient(double sigma) {
		return Features.create(SingleGradientFeature.class, sigma);
	}

	public static FeatureOp lipschitz(double slope, long border) {
		return Features.create(SingleLipschitzFeature.class, slope, border);
	}

	public static FeatureOp hessian(double sigma) {
		return Features.create(SingleHessianFeature.class, sigma);
	}

	public static FeatureOp differenceOfGaussians(double sigma1, double sigma2) {
		return Features.create(SingleDifferenceOfGaussiansFeature.class, sigma1, sigma2);
	}
}
