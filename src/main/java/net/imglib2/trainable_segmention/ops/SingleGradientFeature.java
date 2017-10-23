package net.imglib2.trainable_segmention.ops;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.converter.Converters;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.operators.SetZero;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * Created by arzt on 19.07.17.
 */
@Plugin(type = FeatureOp.class)
public class SingleGradientFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		calculate(in, out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Gradient_filter_" + sigma);
	}

	private void calculate(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		int numDimensions = out.numDimensions();

		Interval expand = Intervals.expand(out, RevampUtils.nCopies(numDimensions, 1));
		RandomAccessibleInterval<FloatType> blurred = gauss(in, expand, 0.4 * sigma);
		RandomAccessibleInterval<FloatType> derivative = ops().create().img(out);

		setZero(out);
		for (int d = 0; d < numDimensions; d++) {
			PartialDerivative.gradientCentralDifference2(blurred, derivative, d);
			add(out, squared(derivative));
		}
		Views.iterable(out).forEach(x -> x.set((float) Math.sqrt(x.get())));
	}

	private RandomAccessibleInterval<FloatType> gauss(RandomAccessible<FloatType> in, Interval outputInterval, double sigma) {
		RandomAccessibleInterval<FloatType> blurred = ops().create().img(outputInterval, new FloatType());
		try {
			Gauss3.gauss(sigma, in, blurred);
		} catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
		return blurred;
	}

	private void setZero(RandomAccessibleInterval<? extends SetZero> out) {
		Views.iterable(out).forEach(SetZero::setZero);
	}

	private RandomAccessibleInterval<FloatType> squared(RandomAccessibleInterval<FloatType> derivate) {
		return Converters.convert(derivate, (i, o) -> {
			o.set(i);
			o.mul(i);
		}, new FloatType());
	}

	private void add(RandomAccessibleInterval<FloatType> out, RandomAccessible<FloatType> in) {
		Views.interval(Views.pair(out, in), out).forEach(p -> p.getA().add(p.getB()));
	}
}
