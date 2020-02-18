
package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.imglib2.trainable_segmention.pixel_feature.filter.dog2.DifferenceOfGaussiansFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gauss.GaussianBlurFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.GaussianGradientMagnitudeFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.HessianEigenvaluesFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.laplacian.LaplacianOfGaussianFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.lipschitz.LipschitzFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.stats.SingleSphereShapedFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gabor.GaborFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.SobelGradientFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.stats.SphereShapedFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.stats.StatisticsFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.structure.StructureTensorEigenvaluesFeature;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @author Matthias Arzt
 */
public class GroupedFeatures {

	public static FeatureSetting gabor() {
		return createFeature(GaborFeature.class, "legacyNormalize", FALSE);
	}

	public static FeatureSetting legacyGabor() {
		return createFeature(GaborFeature.class, "legacyNormalize", TRUE);
	}

	public static FeatureSetting gauss() {
		return createFeature(GaussianBlurFeature.class);
	}

	public static FeatureSetting sobelGradient() {
		return createFeature(SobelGradientFeature.class);
	}

	public static FeatureSetting gradient() {
		return createFeature(GaussianGradientMagnitudeFeature.class);
	}

	private static FeatureSetting createSphereShapeFeature(String operation) {
		return createFeature(SphereShapedFeature.class, "operation", operation);
	}

	public static FeatureSetting laplacian() {
		return createFeature(LaplacianOfGaussianFeature.class);
	}

	public static FeatureSetting lipschitz(long border) {
		return createFeature(LipschitzFeature.class, "border", border);
	}

	public static FeatureSetting hessian() {
		return createFeature(HessianEigenvaluesFeature.class);
	}

	public static FeatureSetting differenceOfGaussians() {
		return createFeature(DifferenceOfGaussiansFeature.class);
	}

	public static FeatureSetting structure() {
		return createFeature(StructureTensorEigenvaluesFeature.class);
	}

	public static FeatureSetting statistics() {
		return statistics(true, true, true, true);
	}

	public static FeatureSetting min() {
		return statistics(true, false, false, false);
	}

	public static FeatureSetting max() {
		return statistics(false, true, false, false);
	}

	public static FeatureSetting mean() {
		return statistics(false, false, true, false);
	}

	public static FeatureSetting variance() {
		return statistics(false, false, false, true);
	}

	private static FeatureSetting statistics(boolean min, boolean max, boolean mean,
		boolean variance)
	{
		return createFeature(StatisticsFeature.class, "min", min, "max", max, "mean", mean, "variance",
			variance);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
