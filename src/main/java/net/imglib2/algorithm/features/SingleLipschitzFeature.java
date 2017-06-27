package net.imglib2.algorithm.features;

import net.imglib2.*;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.*;
import java.util.Iterator;

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
		forward(in, out0);
		backward(Views.extendBorder(out0), (Interval) out0);
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Lipschitz_" + slope);
	}

	<T extends RealType<T>> void forward(RandomAccessible<T> in, RandomAccessibleInterval<T> out) {
		RandomAccess<T> outCursor = out.randomAccess();
		RandomAccess<Neighborhood<T>> inRa = shape.neighborhoodsRandomAccessible(in).randomAccess();
		NeighborhoodOperation no = new NeighborhoodOperation(slope, out.numDimensions(), shape);
		for (Localizable location : BackwardIterate.iterable(out)) {
			outCursor.setPosition(location);
			inRa.setPosition(outCursor);
			outCursor.get().setReal(no.inspect(inRa.get(), outCursor));
		}
	}

	<T extends RealType<T>> void backward(RandomAccessible<T> inOut, Interval interval) {
		RandomAccess<T> out = inOut.randomAccess();
		RandomAccess<Neighborhood<T>> in = shape.neighborhoodsRandomAccessible(inOut).randomAccess();
		NeighborhoodOperation no = new NeighborhoodOperation(slope, out.numDimensions(), shape);
		for (Localizable location : BackwardIterate.iterable(interval)) {
			in.setPosition(location);
			out.setPosition(location);
			out.get().setReal(no.inspect(in.get(), out));
		}
	}

	private static class NeighborhoodOperation {

		private final double slope;

		private final List<Double> offsets;

		private NeighborhoodOperation(double slope, int numDimensions, Shape shape) {
			this.slope = slope;
			offsets = calculateOffsets(shape, numDimensions);
		}

		private List<Double> calculateOffsets(Shape shape, int numDimensions) {
			RandomAccess<Neighborhood<FloatType>> randomAccess = shape.neighborhoodsRandomAccessible(
					ArrayImgs.floats(RevampUtils.nCopies(numDimensions, 1))).randomAccess();
			Neighborhood<?> neighborhood = randomAccess.get();
			Cursor<?> cursor = neighborhood.cursor();
			List<Double> result = new ArrayList<>();
			while (cursor.hasNext()) {
				cursor.fwd();
				result.add(- slope * distance(randomAccess, cursor));
			}
			return result;
		}

		private <T extends RealType<T>> double inspect(Neighborhood<T> neighborhood, Localizable center) {
			Cursor<T> neighbor = neighborhood.localizingCursor();
			Iterator<Double> offset = offsets.iterator();
			double max = Double.NEGATIVE_INFINITY;
			while (neighbor.hasNext()) {
				neighbor.fwd();
				double v1 = neighbor.get().getRealDouble() + offset.next();
				if (v1 > max)
					max = v1;
			}
			return max;
		}
	}

	private static double distance(Localizable a, Localizable b) {
		int n = a.numDimensions();
		long sum = 0;
		for (int i = 0; i < n; i++) {
			long difference = a.getLongPosition(i) - b.getLongPosition(i);
			sum += difference * difference;
		}
		return Math.sqrt(sum);
	}
}
