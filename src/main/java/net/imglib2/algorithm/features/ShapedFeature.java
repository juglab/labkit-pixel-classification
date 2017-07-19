package net.imglib2.algorithm.features;

import net.imagej.ops.Ops;
import net.imglib2.algorithm.features.ops.SphereShapedFeature;
import net.imglib2.algorithm.neighborhood.Shape;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class ShapedFeature {

	private ShapedFeature() { }

	public static Feature min() {
		return multipleSphereFeature(SphereShapedFeature.MIN);
	}

	public static Feature max() {
		return multipleSphereFeature(SphereShapedFeature.MAX);
	}

	public static Feature mean() {
		return multipleSphereFeature(SphereShapedFeature.MEAN);
	}

	public static Feature median() {
		return multipleSphereFeature(SphereShapedFeature.MEDIAN);
	}

	public static Feature variance() {
		return multipleSphereFeature(SphereShapedFeature.VARIANCE);
	}

	// -- Helper constants --

	private static final List<Double> RADIUS = Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0);

	// -- Helper methods --

	private static Feature multipleSphereFeature(String label) {
		List<Feature> features = RADIUS.stream()
				.map(r -> singleShapedFeature(r, label))
				.collect(Collectors.toList());
		return new FeatureGroup(features);
	}

	private static Feature singleShapedFeature(double raduis, String operation) {
		return Features.create(SphereShapedFeature.class, raduis, operation);
	}

}
