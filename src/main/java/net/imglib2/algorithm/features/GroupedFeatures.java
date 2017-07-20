package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleSphereShapedFeature;
import net.imglib2.algorithm.features.ops.SobelGradientFeature;
import net.imglib2.algorithm.features.ops.SphereShapedFeature;

/**
 * Created by arzt on 20.07.17.
 */
public class GroupedFeatures {

	private GroupedFeatures() {
		// prevent from being instantiated
	}

	public static FeatureOp gabor() {
		boolean legacyNormalize = false;
		return Features.create(net.imglib2.algorithm.features.ops.GaborFeature.class, legacyNormalize);
	}

	public static FeatureOp legacyGabor() {
		boolean legacyNormalize = true;
		return Features.create(net.imglib2.algorithm.features.ops.GaborFeature.class, legacyNormalize);
	}

	public static FeatureOp gauss() {
		return Features.create(net.imglib2.algorithm.features.ops.GaussFeature.class);
	}

	public static FeatureOp sobelGradient() {
		return Features.create(SobelGradientFeature.class);
	}

	public static FeatureOp gradient() {
		return Features.create(net.imglib2.algorithm.features.ops.GradientFeature.class);
	}

	public static FeatureOp min() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MIN);
	}

	public static FeatureOp max() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MAX);
	}

	public static FeatureOp mean() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MEAN);
	}

	public static FeatureOp median() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MEDIAN);
	}

	public static FeatureOp variance() {
		return createSphereShapeFeature(SingleSphereShapedFeature.VARIANCE);
	}

	private static FeatureOp createSphereShapeFeature(String operation) {
		return Features.create(SphereShapedFeature.class, operation);
	}

	public static FeatureOp lipschitz(long border) {
		return Features.create(net.imglib2.algorithm.features.ops.LipschitzFeature.class, border);
	}

	public static FeatureOp hessian() {
		return Features.create(net.imglib2.algorithm.features.ops.HessianFeature.class);
	}

	public static FeatureOp differenceOfGaussians() {
		return Features.create(net.imglib2.algorithm.features.ops.DifferenceOfGaussiansFeature.class);
	}
}
