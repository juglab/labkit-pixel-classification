
package net.imglib2.trainable_segmentation.pixel_feature.filter;

import net.imglib2.trainable_segmentation.pixel_feature.filter.dog2.DifferenceOfGaussiansFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.gauss.GaussianBlurFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.gradient.GaussianGradientMagnitudeFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.hessian.HessianEigenvaluesFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.laplacian.LaplacianOfGaussianFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.stats.MaxFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.stats.MeanFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.stats.VarianceFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.lipschitz.LipschitzFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gabor.GaborFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.stats.MinFeature;
import net.imglib2.trainable_segmentation.pixel_feature.filter.structure.StructureTensorEigenvaluesFeature;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSetting;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @author Matthias Arzt
 */
public class GroupedFeatures {

	@Deprecated
	public static FeatureSetting gabor() {
		return createFeature(GaborFeature.class, "legacyNormalize", FALSE);
	}

	@Deprecated
	public static FeatureSetting legacyGabor() {
		return createFeature(GaborFeature.class, "legacyNormalize", TRUE);
	}

	public static FeatureSetting gauss() {
		return createFeature(GaussianBlurFeature.class);
	}

	public static FeatureSetting gradient() {
		return createFeature(GaussianGradientMagnitudeFeature.class);
	}

	public static FeatureSetting laplacian() {
		return createFeature(LaplacianOfGaussianFeature.class);
	}

	@Deprecated
	public static FeatureSetting lipschitz(long border) {
		return createFeature(LipschitzFeature.class, "border", border);
	}

	public static FeatureSetting hessian() {
		return createFeature(HessianEigenvaluesFeature.class);
	}

	public static FeatureSetting differenceOfGaussians() {
		return createFeature(DifferenceOfGaussiansFeature.class);
	}

	public static FeatureSetting structureTensor() {
		return createFeature(StructureTensorEigenvaluesFeature.class);
	}

	public static FeatureSetting min() {
		return createFeature(MinFeature.class);
	}

	public static FeatureSetting max() {
		return createFeature(MaxFeature.class);
	}

	public static FeatureSetting mean() {
		return createFeature(MeanFeature.class);
	}

	public static FeatureSetting variance() {
		return createFeature(VarianceFeature.class);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
