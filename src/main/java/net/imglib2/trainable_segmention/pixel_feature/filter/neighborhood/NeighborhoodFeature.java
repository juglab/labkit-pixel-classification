package net.imglib2.trainable_segmention.pixel_feature.filter.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Plugin( type = FeatureOp.class, label = "Neighborhood" )
public class NeighborhoodFeature extends AbstractFeatureOp {

	@Parameter
	int radius = 1;

	@Override
	public int count() {
		return initOffsets().size();
	}

	private List<Localizable> initOffsets() {
		final RectangleShape shape = new RectangleShape(radius, true);
		final Neighborhood<?> neighborhood = getNeighborhood(shape);
		return getNeighborhoodOffsets(neighborhood);
	}

	private Neighborhood<?> getNeighborhood(Shape shape) {
		final int n = globalSettings().numDimensions();
		final RandomAccessible<BoolType> dummy = ConstantUtils.constantRandomAccessible(new BoolType(), n);
		RandomAccess<Neighborhood<BoolType>> ra = shape.neighborhoodsRandomAccessible(dummy).randomAccess();
		ra.setPosition(new long[n]);
		return ra.get();
	}

	private List<Localizable> getNeighborhoodOffsets(Neighborhood<?> neighborhood) {
		Cursor<?> cursor = neighborhood.cursor();
		List<Localizable> result = new ArrayList<>();
		while (cursor.hasNext()) {
			cursor.fwd();
			result.add(new Point(cursor));
		}
		return result;
	}

	@Override
	public List<String> attributeLabels() {
		return initOffsets().stream().map(p -> "Neighbor " + p).collect(Collectors.toList());
	}

	@Override
	public List<RandomAccessibleInterval<FloatType>> apply(FeatureInput in) {
		final Interval interval = in.targetInterval();
		return initOffsets().stream().map(offset -> copy(Views.interval(Views.translate(in.original(), negate(offset)), interval))).collect(Collectors.toList());
	}

	private RandomAccessibleInterval<FloatType> copy(IntervalView<FloatType> interval) {
		RandomAccessibleInterval<FloatType> result = Views.translate(ArrayImgs.floats(Intervals.dimensionsAsLongArray(interval)), Intervals.minAsLongArray(interval));
		LoopBuilder.setImages(interval, result).forEachPixel( (i,o) -> o.set(i) );
		return result;
	}

	private long[] negate(Localizable localizable) {
		long[] result = new long[localizable.numDimensions()];
		localizable.localize(result);
		for (int i = 0; i < result.length; i++) result[i] = -result[i];
		return result;
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		throw new UnsupportedOperationException();
	}
}
