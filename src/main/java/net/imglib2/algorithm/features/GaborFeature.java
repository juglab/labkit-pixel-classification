package net.imglib2.algorithm.features;


import net.imglib2.*;
import net.imglib2.algorithm.features.ops.FeatureOp;
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

	public static FeatureOp group() {
		return Features.create(net.imglib2.algorithm.features.ops.GaborFeature.class);
	}

	public static FeatureOp single(double sigma, double gamma, double psi, double frequency, int nAngles) {
		return Features.create(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles);
	}

	public static FeatureOp legacy() {
		boolean legazyNormalize = true;
		return Features.create(net.imglib2.algorithm.features.ops.GaborFeature.class, legazyNormalize);
	}

	public static FeatureOp singleLegacy(double sigma, double gamma, double psi, double frequency, int nAngles) {
		boolean legazyNormalize = true;
		return Features.create(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles, legazyNormalize);
	}

}
