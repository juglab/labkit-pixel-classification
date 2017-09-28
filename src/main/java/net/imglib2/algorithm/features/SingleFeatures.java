package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.IdendityFeature;
import net.imglib2.algorithm.features.ops.SingleDifferenceOfGaussiansFeature;
import net.imglib2.algorithm.features.ops.SingleGaborFeature;
import net.imglib2.algorithm.features.ops.SingleGaussFeature;
import net.imglib2.algorithm.features.ops.SingleGradientFeature;
import net.imglib2.algorithm.features.ops.SingleHessianFeature;
import net.imglib2.algorithm.features.ops.SingleLipschitzFeature;
import net.imglib2.algorithm.features.ops.SingleSobelGradientFeature;
import net.imglib2.algorithm.features.ops.SingleSphereShapedFeature;

/**
 * @author Matthias Arzt
 */
public class SingleFeatures {

	public static FeatureSetting identity() {
		return createFeature(IdendityFeature.class);
	}

	public static FeatureSetting gabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		return gabor(sigma, gamma, psi, frequency, nAngles, false);
	}

	public static FeatureSetting legacyGabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		return gabor(sigma, gamma, psi, frequency, nAngles, true);
	}

	private static FeatureSetting gabor(double sigma, double gamma, double psi, double frequency, int nAngles, boolean legacyNormalize) {
		return createFeature(SingleGaborFeature.class, "sigma", sigma, "gamma", gamma, "psi", psi,
				"frequency", frequency, "nAngles", nAngles, "legacyNormalize", legacyNormalize);
	}

	public static FeatureSetting gauss(double sigma) {
		return createFeature(SingleGaussFeature.class, "sigma", sigma);
	}

	public static FeatureSetting sobelGradient(double sigma) {
		return createFeature(SingleSobelGradientFeature.class, "sigma", sigma);
	}

	public static FeatureSetting gradient(double sigma) {
		return createFeature(SingleGradientFeature.class, "sigma", sigma);
	}

	public static FeatureSetting lipschitz(double slope, long border) {
		return createFeature(SingleLipschitzFeature.class, "slope", slope, "border", border);
	}

	public static FeatureSetting hessian(double sigma) {
		return createFeature(SingleHessianFeature.class, "sigma", sigma);
	}

	public static FeatureSetting differenceOfGaussians(double sigma1, double sigma2) {
		return createFeature(SingleDifferenceOfGaussiansFeature.class, "sigma1", sigma1, "sigma2", sigma2);
	}

	public static FeatureSetting sphereOperation(double radius, String operation) {
		return createFeature(SingleSphereShapedFeature.class, "radius", radius, "operation", operation);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
