
package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.SingleGaussianGradientMagnitudeFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.identity.IdendityFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.dog2.SingleDifferenceOfGaussiansFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gabor.SingleGaborFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gauss.SingleGaussianBlurFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.SingleHessian3DFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.SingleHessianFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.lipschitz.SingleLipschitzFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.SingleSobelGradientFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.stats.SingleSphereShapedFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.stats.SingleStatisticsFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.structure.SingleStructureFeature3D;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;

/**
 * @author Matthias Arzt
 */
public class SingleFeatures {

	public static FeatureSetting identity() {
		return createFeature(IdendityFeature.class);
	}

	public static FeatureSetting gabor(double sigma, double gamma, double psi, double frequency,
		int nAngles)
	{
		return gabor(sigma, gamma, psi, frequency, nAngles, false);
	}

	public static FeatureSetting legacyGabor(double sigma, double gamma, double psi, double frequency,
		int nAngles)
	{
		return gabor(sigma, gamma, psi, frequency, nAngles, true);
	}

	private static FeatureSetting gabor(double sigma, double gamma, double psi, double frequency,
		int nAngles, boolean legacyNormalize)
	{
		return createFeature(SingleGaborFeature.class, "sigma", sigma, "gamma", gamma, "psi", psi,
			"frequency", frequency, "nAngles", nAngles, "legacyNormalize", legacyNormalize);
	}

	public static FeatureSetting gauss(double sigma) {
		return createFeature(SingleGaussianBlurFeature.class, "sigma", sigma);
	}

	public static FeatureSetting sobelGradient(double sigma) {
		return createFeature(SingleSobelGradientFeature.class, "sigma", sigma);
	}

	public static FeatureSetting gradient(double sigma) {
		return createFeature(SingleGaussianGradientMagnitudeFeature.class, "sigma", sigma);
	}

	public static FeatureSetting lipschitz(double slope, long border) {
		return createFeature(SingleLipschitzFeature.class, "slope", slope, "border", border);
	}

	public static FeatureSetting hessian(double sigma) {
		return createFeature(SingleHessianFeature.class, "sigma", sigma);
	}

	public static FeatureSetting differenceOfGaussians(double sigma1, double sigma2) {
		return createFeature(SingleDifferenceOfGaussiansFeature.class, "sigma1", sigma1, "sigma2",
			sigma2);
	}

	public static FeatureSetting sphereOperation(double radius, String operation) {
		return createFeature(SingleSphereShapedFeature.class, "radius", radius, "operation", operation);
	}

	public static FeatureSetting hessian3d(double sigma, boolean absoluteValues) {
		return createFeature(SingleHessian3DFeature.class, "sigma", sigma, "absoluteValues",
			absoluteValues);
	}

	public static FeatureSetting structure(double sigma, double integrationScale) {
		return createFeature(SingleStructureFeature3D.class, "sigma", sigma, "integrationScale",
			integrationScale);
	}

	public static FeatureSetting statistics(double radius, boolean min, boolean max, boolean mean,
		boolean variance)
	{
		return createFeature(SingleStatisticsFeature.class, "radius", radius, "min", min, "max", max,
			"mean", mean, "variance", variance);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
