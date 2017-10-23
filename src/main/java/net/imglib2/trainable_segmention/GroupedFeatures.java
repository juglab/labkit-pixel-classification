package net.imglib2.trainable_segmention;

import net.imglib2.trainable_segmention.ops.DifferenceOfGaussiansFeature;
import net.imglib2.trainable_segmention.ops.FeatureOp;
import net.imglib2.trainable_segmention.ops.GaussFeature;
import net.imglib2.trainable_segmention.ops.GradientFeature;
import net.imglib2.trainable_segmention.ops.LipschitzFeature;
import net.imglib2.trainable_segmention.ops.SingleSphereShapedFeature;
import net.imglib2.trainable_segmention.ops.GaborFeature;
import net.imglib2.trainable_segmention.ops.SobelGradientFeature;
import net.imglib2.trainable_segmention.ops.SphereShapedFeature;
import net.imglib2.trainable_segmention.ops.Hessian3DFeature;
import net.imglib2.trainable_segmention.ops.HessianFeature;

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
		return createFeature(GaussFeature.class);
	}

	public static FeatureSetting sobelGradient() {
		return createFeature(SobelGradientFeature.class);
	}

	public static FeatureSetting gradient() {
		return createFeature(GradientFeature.class);
	}

	public static FeatureSetting min() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MIN);
	}

	public static FeatureSetting max() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MAX);
	}

	public static FeatureSetting mean() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MEAN);
	}

	public static FeatureSetting median() {
		return createSphereShapeFeature(SingleSphereShapedFeature.MEDIAN);
	}

	public static FeatureSetting variance() {
		return createSphereShapeFeature(SingleSphereShapedFeature.VARIANCE);
	}

	private static FeatureSetting createSphereShapeFeature(String operation) {
		return createFeature(SphereShapedFeature.class, "operation", operation);
	}

	public static FeatureSetting lipschitz(long border) {
		return createFeature(LipschitzFeature.class, "border", border);
	}

	public static FeatureSetting hessian() {
		return createFeature(HessianFeature.class);
	}

	public static FeatureSetting differenceOfGaussians() {
		return createFeature(DifferenceOfGaussiansFeature.class);
	}

	public static FeatureSetting hessian3D(boolean absoluteValues) {
		return createFeature(Hessian3DFeature.class, "absoluteValues", absoluteValues);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
