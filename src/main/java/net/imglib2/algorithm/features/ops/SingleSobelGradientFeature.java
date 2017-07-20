package net.imglib2.algorithm.features.ops;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * ImgLib2 version of trainable segmentation's Sobel feature.
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class SingleSobelGradientFeature extends AbstractFeatureOp {

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
		return Collections.singletonList("Sobel_filter_" + sigma);
	}

	private void calculate(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		double[] sigmas = {0.4 * sigma, 0.4 * sigma};

		Interval expandedInterval = Intervals.expand(out, new long[]{40, 40}); // FIXME how much do we need to extend?

		RandomAccessibleInterval<FloatType> blurred = RevampUtils.gauss(Views.interval(in, expandedInterval), sigmas);
		RandomAccessible<FloatType> dx = RevampUtils.deriveX(blurred);
		RandomAccessible<FloatType> dy = RevampUtils.deriveY(blurred);
		RandomAccessible<Pair<FloatType, FloatType>> derivatives = Views.pair(dx, dy);
		mapToFloat(derivatives, out, input -> norm2(input.getA().get(), input.getB().get()));
	}

	private <I> void mapToFloat(RandomAccessible<I> in, RandomAccessibleInterval<FloatType> out, ToDoubleFunction<I> operation) {
		Views.interval(Views.pair(in, out), out)
				.forEach(p -> p.getB().set((float) operation.applyAsDouble(p.getA())));
	}

	private static double norm2(float x, float y) {
		return Math.sqrt(x * x + y * y);
	}
}
