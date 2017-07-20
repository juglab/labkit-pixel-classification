package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleSphereShapedFeature;
import net.imglib2.algorithm.features.ops.SphereShapedFeature;

/**
 * @author Matthias Arzt
 */
public class ShapedFeature {

	private ShapedFeature() { }

	public static FeatureOp min() {
		return multipleSphereFeature(SingleSphereShapedFeature.MIN);
	}

	public static FeatureOp max() {
		return multipleSphereFeature(SingleSphereShapedFeature.MAX);
	}

	public static FeatureOp mean() {
		return multipleSphereFeature(SingleSphereShapedFeature.MEAN);
	}

	public static FeatureOp median() {
		return multipleSphereFeature(SingleSphereShapedFeature.MEDIAN);
	}

	public static FeatureOp variance() {
		return multipleSphereFeature(SingleSphereShapedFeature.VARIANCE);
	}

	// -- Helper methods --

	private static FeatureOp multipleSphereFeature(String operation) {
		return Features.create(SphereShapedFeature.class, operation);
	}

	private static FeatureOp singleShapedFeature(double raduis, String operation) {
		return Features.create(SingleSphereShapedFeature.class, raduis, operation);
	}

}
