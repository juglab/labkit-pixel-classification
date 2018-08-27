package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Min/Max/Mean/Median/Variance")
public class SingleSphereShapedFeature extends AbstractFeatureOp {

	public static final String MIN = "Minimum";

	public static final String MAX = "Maximum";

	public static final String MEAN = "Mean";

	public static final String MEDIAN = "Median";

	public static final String VARIANCE = "Variance";

	@Parameter
	private double radius;

	@Parameter(choices = {MAX, MIN, MEAN, MEDIAN, VARIANCE})
	private String operation;

	private static Class<? extends Op> getOpClass(String operation) {
		if(operation.equals(MIN)) return Ops.Stats.Min.class;
		if(operation.equals(MAX)) return Ops.Stats.Max.class;
		if(operation.equals(MEAN)) return Ops.Stats.Mean.class;
		if(operation.equals(MEDIAN)) return Ops.Stats.Median.class;
		if(operation.equals(VARIANCE)) return Ops.Stats.Variance.class;
		throw new IllegalArgumentException();
	}

	private UnaryComputerOp<Iterable, DoubleType> getComputer() {
		return Computers.unary(ops(), getOpClass(operation), DoubleType.class, Iterable.class);
	}

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(FeatureInput in, List<RandomAccessibleInterval<FloatType>> out) {
		applySingle(in.original(), out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList(operation + "_" + radius);
	}

	private void applySingle(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		UnaryComputerOp<Iterable, DoubleType> computer = getComputer();
		RandomAccessible<Neighborhood<FloatType>> neighborhoods = new HyperSphereShape((long) radius).neighborhoodsRandomAccessible(in);
		DoubleType tmp = new DoubleType();
		Views.interval(Views.pair(neighborhoods, out), out).forEach(p -> { computer.compute(p.getA(), tmp); p.getB().set(tmp.getRealFloat()); });
	}

}
