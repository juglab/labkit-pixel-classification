package net.imglib2.algorithm.features;


import net.imglib2.*;
import net.imglib2.algorithm.features.ops.SingleGaborFeature;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class GaborFeature {

	private GaborFeature() {
		// prevent from being instantiated
	}

	public static Feature group() {
		return new FeatureGroup(initFeatures(x -> {}));
	}

	public static Feature single(double sigma, double gamma, double psi, double frequency, int nAngles) {
		return createGaborFeature(sigma, gamma, psi, frequency, nAngles, x -> {});
	}

	public static Feature legacy() {
		return new FeatureGroup(initFeatures(GaborFeature::normalize));
	}

	public static Feature singleLegacy(double sigma, double gamma, double psi, double frequency, int nAngles) {
		return createGaborFeature(sigma, gamma, psi, frequency, nAngles, GaborFeature::normalize);
	}

	private static Feature createGaborFeature(double sigma, double gamma, double psi, double frequency, int nAngles, Consumer<RandomAccessibleInterval<FloatType>> postProcessSlice) {
		SingleGaborFeature gaborFeature = Features.create(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles);
		gaborFeature.setPostProcessSlice(postProcessSlice);
		return gaborFeature;
	}

	private static List<Feature> initFeatures(Consumer<RandomAccessibleInterval<FloatType>> postProcessSlice) {
		int nAngles = 10;
		List<Feature> features = new ArrayList<>();
		for(int i=0; i < 2; i++)
			for(double gamma = 1; gamma >= 0.25; gamma /= 2)
				for(int frequency = 2; frequency<3; frequency ++)
				{
					final double psi = Math.PI / 2 * i;
					features.add(createGaborFeature(1.0, gamma, psi, frequency, nAngles, postProcessSlice ));
				}
		// elongated filters in x- axis (sigma = [2.0 - 4.0], gamma = [1.0 - 2.0])
		for(int i=0; i < 2; i++)
			for(double sigma = 2.0; sigma <= 4.0; sigma *= 2)
				for(double gamma = 1.0; gamma <= 2.0; gamma *= 2)
					for(int frequency = 2; frequency<=3; frequency ++)
					{
						final double psi = Math.PI / 2 * i;
						features.add(createGaborFeature(sigma, gamma, psi, frequency, nAngles, postProcessSlice ));
					}
		return features;
	}

	public static void normalize(RandomAccessibleInterval<FloatType> image2) {
		DoubleType mean = RevampUtils.ops().stats().mean(Views.iterable(image2));
		DoubleType stdDev = RevampUtils.ops().stats().stdDev(Views.iterable(image2));
		float mean2 = (float) mean.get();
		float invStdDev = (stdDev.get() == 0) ? 1 : (float) (1 / stdDev.get());
		Views.iterable(image2).forEach(value -> value.set((value.get() - mean2) * invStdDev));
	}

}
