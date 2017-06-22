package net.imglib2.algorithm.features;

import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class ShapedFeatures {

	private ShapedFeatures() { }

	public static Feature singleMin(String label, Shape shape) {
		return singleShapedFeature(label, shape, Ops.Stats.Min.class);
	}

	public static Feature singleMax(String label, Shape shape) {
		return singleShapedFeature(label, shape, Ops.Stats.Max.class);
	}

	public static Feature singleMean(String label, Shape shape) {
		return singleShapedFeature(label, shape, Ops.Stats.Mean.class);
	}

	public static Feature singleMedian(String label, Shape shape) {
		return singleShapedFeature(label, shape, Ops.Stats.Median.class);
	}

	public static Feature singleVariance(String label, Shape shape) {
		return singleShapedFeature(label, shape, Ops.Stats.Variance.class);
	}

	private static Feature singleShapedFeature(String label, Shape shape, Class<? extends Op> operation) {
		UnaryComputerOp<Iterable, DoubleType> computer = Computers.unary(RevampUtils.ops(), operation, DoubleType.class, Iterable.class);
		return new ShapedFeature(label, shape, (in, out) -> out.setReal(computer.run(in, new DoubleType()).get()));
	}

	public static Feature min() {
		return multipleSphereFeature("Minimum", ShapedFeatures::singleMin);
	}

	public static Feature max() {
		return multipleSphereFeature("Maximum", ShapedFeatures::singleMax);
	}

	public static Feature mean() {
		return multipleSphereFeature("Mean", ShapedFeatures::singleMean);
	}

	public static Feature median() {
		return multipleSphereFeature("Median", ShapedFeatures::singleMedian);
	}

	public static Feature variance() {
		return multipleSphereFeature("Variance", ShapedFeatures::singleVariance);
	}

	// -- Helper constants --

	private static final List<Double> RADIUS = Arrays.asList(new Double[]{1.0, 2.0, 4.0, 8.0, 16.0});

	// -- Helper methods --

	private static Feature multipleSphereFeature(String label, BiFunction<String, Shape, Feature> featureFactory) {
		List<Feature> features = RADIUS.stream()
				.map(r -> featureFactory.apply(label + "_" + r, new HyperSphereShape(r.longValue())))
				.collect(Collectors.toList());
		return new FeatureGroup(features);
	}
}
