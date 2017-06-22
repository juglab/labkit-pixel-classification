package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Matthias Arzt
 */
public class ShapedFeature implements Feature {

	private final Shape shape;

	private final BiConsumer<Neighborhood<FloatType>, FloatType> operation;

	private final String label;

	public ShapedFeature(String label, Shape shape, BiConsumer<Neighborhood<FloatType>, FloatType> operation) {
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
