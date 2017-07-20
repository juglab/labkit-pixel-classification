package net.imglib2.algorithm.features;


import net.imglib2.*;
import net.imglib2.algorithm.features.ops.SingleGaborFeature;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * @author Matthias Arzt
 */
public class GaborFeature {

	private GaborFeature() {
		// prevent from being instantiated
	}

	public static Feature group() {
		return Features.create(net.imglib2.algorithm.features.ops.GaborFeature.class);
	}

	public static Feature single(double sigma, double gamma, double psi, double frequency, int nAngles) {
		return Features.create(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles);
	}

	public static Feature legacy() {
		net.imglib2.algorithm.features.ops.GaborFeature feature = Features.create(net.imglib2.algorithm.features.ops.GaborFeature.class);
		feature.setPostProcessSlice(GaborFeature::normalize);
		return feature;
	}

	public static Feature singleLegacy(double sigma, double gamma, double psi, double frequency, int nAngles) {
		SingleGaborFeature gaborFeature = Features.create(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles);
		gaborFeature.setPostProcessSlice(GaborFeature::normalize);
		return gaborFeature;
	}

	public static void normalize(RandomAccessibleInterval<FloatType> image2) {
		DoubleType mean = RevampUtils.ops().stats().mean(Views.iterable(image2));
		DoubleType stdDev = RevampUtils.ops().stats().stdDev(Views.iterable(image2));
		float mean2 = (float) mean.get();
		float invStdDev = (stdDev.get() == 0) ? 1 : (float) (1 / stdDev.get());
		Views.iterable(image2).forEach(value -> value.set((value.get() - mean2) * invStdDev));
	}

}
