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
		return createFeature(net.imglib2.algorithm.features.ops.GaborFeature.class, legacyNormalize);
	}

	public static FeatureOp legacyGabor() {
		boolean legacyNormalize = true;
		return createFeature(net.imglib2.algorithm.features.ops.GaborFeature.class, legacyNormalize);
	}

	public static FeatureOp gauss() {
		return createFeature(net.imglib2.algorithm.features.ops.GaussFeature.class);
	}

	public static FeatureOp sobelGradient() {
		return createFeature(SobelGradientFeature.class);
	}

	public static FeatureOp gradient() {
		return createFeature(net.imglib2.algorithm.features.ops.GradientFeature.class);
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
		return createFeature(SphereShapedFeature.class, operation);
	}

	public static FeatureOp lipschitz(long border) {
		return createFeature(net.imglib2.algorithm.features.ops.LipschitzFeature.class, border);
	}

	public static FeatureOp hessian() {
		return createFeature(net.imglib2.algorithm.features.ops.HessianFeature.class);
	}

	public static FeatureOp differenceOfGaussians() {
		return createFeature(net.imglib2.algorithm.features.ops.DifferenceOfGaussiansFeature.class);
	}

	private static FeatureOp createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return Features.create(aClass, GlobalSettings.defaultSettings(), args);
	}
}
