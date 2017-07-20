package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.SingleSphereShapedFeature;
import net.imglib2.algorithm.features.ops.SphereShapedFeature;

/**
 * @author Matthias Arzt
 */
public class ShapedFeature {

	private ShapedFeature() { }

	public static Feature min() {
		return multipleSphereFeature(SingleSphereShapedFeature.MIN);
	}

	public static Feature max() {
		return multipleSphereFeature(SingleSphereShapedFeature.MAX);
	}

	public static Feature mean() {
		return multipleSphereFeature(SingleSphereShapedFeature.MEAN);
	}

	public static Feature median() {
		return multipleSphereFeature(SingleSphereShapedFeature.MEDIAN);
	}

	public static Feature variance() {
		return multipleSphereFeature(SingleSphereShapedFeature.VARIANCE);
	}

	// -- Helper methods --

	private static Feature multipleSphereFeature(String operation) {
		return Features.create(SphereShapedFeature.class, operation);
	}

	private static Feature singleShapedFeature(double raduis, String operation) {
		return Features.create(SingleSphereShapedFeature.class, raduis, operation);
	}

}
