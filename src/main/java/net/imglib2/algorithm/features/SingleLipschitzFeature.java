package net.imglib2.algorithm.features;

import net.imglib2.*;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class SingleLipschitzFeature implements Feature {

	private final double slope;

	private final Shape shape = new RectangleShape(1, false);

	public SingleLipschitzFeature(double slope) {
		this.slope = slope;
	}

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		RandomAccessibleInterval<FloatType> out0 = out.get(0);
		forward(in, Views.flatIterable(out0));
		backward(Views.extendBorder(out0), (Interval) out0);
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Lipschitz_" + slope);
	}

	<T extends RealType<T>> void forward(RandomAccessible<T> in, IterableInterval<T> out) {
		Cursor<T> outCursor = out.localizingCursor();
		RandomAccess<Neighborhood<T>> inRa = shape.neighborhoodsRandomAccessible(in).randomAccess();
		while (outCursor.hasNext()) {
			outCursor.fwd();
			inRa.setPosition(outCursor);
			outCursor.get().setReal(inspectNeighbors(inRa.get(), outCursor));
		}
	}

	<T extends RealType<T>> void backward(RandomAccessible<T> inOut, Interval interval) {
		RandomAccess<T> out = inOut.randomAccess();
		RandomAccess<Neighborhood<T>> in = shape.neighborhoodsRandomAccessible(inOut).randomAccess();
		for (Localizable location : BackwardIterate.iterable(interval)) {
			in.setPosition(location);
			out.setPosition(location);
			out.get().setReal(inspectNeighbors(in.get(), out));
		}
	}

	private <T extends RealType<T>> double inspectNeighbors(Neighborhood<T> neighborhood, Localizable center) {
		Cursor<T> neighbor = neighborhood.localizingCursor();
		double max = Double.NEGATIVE_INFINITY;
		while (neighbor.hasNext()) {
			neighbor.fwd();
			double v1 = neighbor.get().getRealDouble() - slope * distance(center, neighbor);
			if (v1 > max)
				max = v1;
		}
		return max;
	}

	private double distance(Localizable a, Localizable b) {
		int n = a.numDimensions();
		long sum = 0;
		for (int i = 0; i < n; i++) {
			long difference = a.getLongPosition(i) - b.getLongPosition(i);
			sum += difference * difference;
		}
		return Math.sqrt(sum);
	}
}
