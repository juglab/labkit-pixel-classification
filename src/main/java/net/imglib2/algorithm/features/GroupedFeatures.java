package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleSphereShapedFeature;
import net.imglib2.algorithm.features.ops.SobelGradientFeature;
import net.imglib2.algorithm.features.ops.SphereShapedFeature;

/**
 * Created by arzt on 20.07.17.
 */
public class GroupedFeatures {

	public static final GroupedFeatures GroupedFeatures = new GroupedFeatures(GlobalSettings.defaultSettings());

	private final GlobalSettings globalSettings;

	public GroupedFeatures(GlobalSettings settings) {
		this.globalSettings = settings;
	}

	public FeatureOp gabor() {
		boolean legacyNormalize = false;
		return createFeature(net.imglib2.algorithm.features.ops.GaborFeature.class, legacyNormalize);
	}

	public FeatureOp legacyGabor() {
		boolean legacyNormalize = true;
		return createFeature(net.imglib2.algorithm.features.ops.GaborFeature.class, legacyNormalize);
	}

	public FeatureOp gauss() {
		return createFeature(net.imglib2.algorithm.features.ops.GaussFeature.class);
	}

	public FeatureOp sobelGradient() {
		return createFeature(SobelGradientFeature.class);
	}

	public FeatureOp gradient() {
		return createFeature(net.imglib2.algorithm.features.ops.GradientFeature.class);
	}

	public FeatureOp min() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MIN);
	}

	public FeatureOp max() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MAX);
	}

	public FeatureOp mean() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MEAN);
	}

	public FeatureOp median() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MEDIAN);
	}

	public FeatureOp variance() {
		return createSphereShapeFeature(SingleSphereShapedFeature.VARIANCE);
	}

	private FeatureOp createSphereShapeFeature(String operation) {
		return createFeature(SphereShapedFeature.class, operation);
	}

	public FeatureOp lipschitz(long border) {
		return createFeature(net.imglib2.algorithm.features.ops.LipschitzFeature.class, border);
	}

	public FeatureOp hessian() {
		return createFeature(net.imglib2.algorithm.features.ops.HessianFeature.class);
	}

	public FeatureOp differenceOfGaussians() {
		return createFeature(net.imglib2.algorithm.features.ops.DifferenceOfGaussiansFeature.class);
	}

	private FeatureOp createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return Features.create(aClass, globalSettings, args);
	}
}
