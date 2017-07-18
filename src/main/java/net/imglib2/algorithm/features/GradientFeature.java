package net.imglib2.algorithm.features;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.converter.Converters;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.operators.SetZero;
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

	public static Feature sobelSignle(double sigma) {
		return new SobelGradientFeature(sigma);
	}

	public static Feature sobelGroup() {
		return new FeatureGroup(Arrays.stream(SIGMAS)
				.mapToObj(GradientFeature::sobelSignle)
				.collect(Collectors.toList()));
	}

	public static Feature single(double sigma) {
		return new SignleGradientFeature(sigma);
	}

	public static Feature group() {
		return new FeatureGroup(Arrays.stream(SIGMAS)
				.mapToObj(GradientFeature::single)
				.collect(Collectors.toList()));
	}

	/**
	 * ImgLib2 version of trainable segmentation's Sobel feature.
	 * @author Matthias Arzt
	 */
	private static class SobelGradientFeature implements Feature {

		private final double sigma;

		public SobelGradientFeature(double sigma) {
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

	private static class SignleGradientFeature implements Feature {

		private final double sigma;

		public SignleGradientFeature(double sigma) {
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
			return Collections.singletonList("Gradient_filter_" + sigma);
		}

		private void calculate(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
			int numDimensions = out.numDimensions();

			Interval expand = Intervals.expand(out, RevampUtils.nCopies(numDimensions, 1));
			RandomAccessibleInterval<FloatType> blurred = gauss(in, expand, 0.4 * sigma);
			RandomAccessibleInterval<FloatType> derivative = RevampUtils.ops().create().img(out);

			setZero(out);
			for(int d = 0; d < numDimensions; d++) {
				PartialDerivative.gradientCentralDifference2(blurred, derivative, d);
				add(out, squared(derivative));
			}
			Views.iterable(out).forEach(x -> x.set((float) Math.sqrt(x.get())));
		}

		private RandomAccessibleInterval<FloatType> gauss(RandomAccessible<FloatType> in, Interval outputInterval, double sigma) {
			RandomAccessibleInterval<FloatType> blurred = RevampUtils.ops().create().img(outputInterval, new FloatType());
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
			return Converters.convert(derivate, (i, o) -> { o.set(i); o.mul(i); }, new FloatType());
		}

		private void add(RandomAccessibleInterval<FloatType> out, RandomAccessible<FloatType> in) {
			Views.interval(Views.pair(out, in), out).forEach(p -> p.getA().add(p.getB()));
		}
	}
}
