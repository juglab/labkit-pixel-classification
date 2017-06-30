package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mattias Arzt
 */
public class DifferenceOfGaussiansFeature {

	private DifferenceOfGaussiansFeature() {
		// prevent from being instantiated
	}

	public static Feature group() {
		return new FeatureGroup(initFeatures());
	}

	private static List<Feature> initFeatures() {
		List<Feature> features = new ArrayList<>();
		final double minimumSigma = 1;
		final double maximumSigma = 16;
		for (double sigma1 = minimumSigma; sigma1 <= maximumSigma; sigma1 *= 2)
			for (double sigma2 = minimumSigma; sigma2 < sigma1; sigma2 *= 2)
				features.add(new SingleDifferenceOfGaussiansFeature(sigma1, sigma2));
		return features;
	}

	private static class SingleDifferenceOfGaussiansFeature implements Feature {

		private final double sigma1;

		private final double sigma2;

		private SingleDifferenceOfGaussiansFeature(double sigma1, double sigma2) {
			this.sigma1 = sigma1;
			this.sigma2 = sigma2;
		}

		@Override
		public int count() {
			return 1;
		}

		@Override
		public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
			dog(in, out.get(0));
		}

		@Override
		public List<String> attributeLabels() {
			return Collections.singletonList("Difference_of_gaussians_" + sigma1 + "_" + sigma2);
		}

		private void dog(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
			try {
				Img<FloatType> tmp = RevampUtils.ops().create().img(out);
				Gauss3.gauss(sigma1 * 0.4, in, tmp);
				Gauss3.gauss(sigma2 * 0.4, in, out);
				Views.interval(Views.pair(tmp, out), out).forEach(p -> p.getB().sub(p.getA()));
			} catch (IncompatibleTypeException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
