package net.imglib2.algorithm.features;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class GradientFeature {

	private static final double[] SIGMAS = new double[]{0.0, 1.0, 2.0, 4.0, 8.0, 16.0};

	private GradientFeature() {
		// prevent from being instantiated
	}

	public static Feature single(double sigma) {
		return new SingleGradientFeature(sigma);
	}

	public static Feature group() {
		return new FeatureGroup(initFeatures());
	}

	private static List<Feature> initFeatures() {
		return Arrays.stream(SIGMAS)
				.mapToObj(GradientFeature::single)
				.collect(Collectors.toList());
	}

	/**
	 * ImgLib2 version of trainable segmentation's Sobel feature.
	 * @author Matthias Arzt
	 */
	private static class SingleGradientFeature implements Feature {

		private final double sigma;

		public SingleGradientFeature(double sigma) {
			this.sigma = sigma;
		}

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
}
