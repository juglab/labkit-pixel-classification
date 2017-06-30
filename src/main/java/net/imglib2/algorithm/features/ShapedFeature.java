package net.imglib2.algorithm.features;

import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class ShapedFeature {

	private ShapedFeature() { }

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
		return new SingleShapedFeature(label, shape, (in, out) -> out.setReal(computer.run(in, new DoubleType()).get()));
	}

	public static Feature min() {
		return multipleSphereFeature("Minimum", ShapedFeature::singleMin);
	}

	public static Feature max() {
		return multipleSphereFeature("Maximum", ShapedFeature::singleMax);
	}

	public static Feature mean() {
		return multipleSphereFeature("Mean", ShapedFeature::singleMean);
	}

	public static Feature median() {
		return multipleSphereFeature("Median", ShapedFeature::singleMedian);
	}

	public static Feature variance() {
		return multipleSphereFeature("Variance", ShapedFeature::singleVariance);
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

	/**
	 * @author Matthias Arzt
	 */
	public static class SingleShapedFeature implements Feature {

		private final Shape shape;

		private final BiConsumer<Neighborhood<FloatType>, FloatType> operation;

		private final String label;

		public SingleShapedFeature(String label, Shape shape, BiConsumer<Neighborhood<FloatType>, FloatType> operation) {
			this.shape = shape;
			this.operation = operation;
			this.label = label;
		}

		@Override
		public int count() {
			return 1;
		}

		@Override
		public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
			applySingle(in, out.get(0));
		}

		@Override
		public List<String> attributeLabels() {
			return Collections.singletonList(label);
		}

		private void applySingle(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
			RandomAccessible<Neighborhood<FloatType>> neighborhoods = shape.neighborhoodsRandomAccessible(in);
			Views.interval(Views.pair(neighborhoods, out), out).forEach(p -> operation.accept(p.getA(), p.getB()));
		}

	}
}
