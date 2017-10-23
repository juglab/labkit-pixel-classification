package net.imglib2.trainable_segmention;

import net.imglib2.trainable_segmention.ops.FeatureOp;
import net.imglib2.trainable_segmention.ops.IdendityFeature;
import net.imglib2.trainable_segmention.ops.SingleDifferenceOfGaussiansFeature;
import net.imglib2.trainable_segmention.ops.SingleGaborFeature;
import net.imglib2.trainable_segmention.ops.SingleGaussFeature;
import net.imglib2.trainable_segmention.ops.SingleGradientFeature;
import net.imglib2.trainable_segmention.ops.SingleHessian3DFeature;
import net.imglib2.trainable_segmention.ops.SingleHessianFeature;
import net.imglib2.trainable_segmention.ops.SingleLipschitzFeature;
import net.imglib2.trainable_segmention.ops.SingleSobelGradientFeature;
import net.imglib2.trainable_segmention.ops.SingleSphereShapedFeature;

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

	public static FeatureSetting hessian3d(double sigma, boolean absoluteValues) {
		return createFeature(SingleHessian3DFeature.class, "sigma", sigma, "absoluteValues", absoluteValues);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
