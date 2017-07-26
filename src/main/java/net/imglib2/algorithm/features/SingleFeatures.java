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
		return createFeature(IdendityFeature.class);
	}

	public static FeatureOp gabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		boolean legacyNormalize = false;
		return createFeature(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles, legacyNormalize);
	}

	public static FeatureOp legacyGabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		boolean legacyNormalize = true;
		return createFeature(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles, legacyNormalize);
	}

	public static FeatureOp gauss(double sigma) {
		return createFeature(SingleGaussFeature.class, sigma);
	}

	public static FeatureOp sobelGradient(double sigma) {
		return createFeature(SingleSobelGradientFeature.class, sigma);
	}

	public static FeatureOp gradient(double sigma) {
		return createFeature(SingleGradientFeature.class, sigma);
	}

	public static FeatureOp lipschitz(double slope, long border) {
		return createFeature(SingleLipschitzFeature.class, slope, border);
	}

	public static FeatureOp hessian(double sigma) {
		return createFeature(SingleHessianFeature.class, sigma);
	}

	public static FeatureOp differenceOfGaussians(double sigma1, double sigma2) {
		return createFeature(SingleDifferenceOfGaussiansFeature.class, sigma1, sigma2);
	}

	private static FeatureOp createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return Features.create(aClass, GlobalSettings.defaultSettings(), args);
	}
}
