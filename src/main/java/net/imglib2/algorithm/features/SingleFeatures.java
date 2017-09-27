package net.imglib2.algorithm.features;

import net.imagej.ops.OpEnvironment;
import net.imglib2.algorithm.features.ops.*;

/**
 * @author Matthias Arzt
 */
public class SingleFeatures {

	private final OpEnvironment ops;

	private final GlobalSettings globalSettings;

	public SingleFeatures(OpEnvironment ops) {
		this(ops, GlobalSettings.defaultSettings());
	}

	public SingleFeatures(OpEnvironment ops, GlobalSettings settings) {
		this.ops = ops;
		this.globalSettings = settings;
	}

	public FeatureOp identity() {
		return createFeature(IdendityFeature.class);
	}

	public FeatureOp gabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		boolean legacyNormalize = false;
		return createFeature(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles, legacyNormalize);
	}

	public FeatureOp legacyGabor(double sigma, double gamma, double psi, double frequency, int nAngles) {
		boolean legacyNormalize = true;
		return createFeature(SingleGaborFeature.class, sigma, gamma, psi, frequency, nAngles, legacyNormalize);
	}

	public FeatureOp gauss(double sigma) {
		return createFeature(SingleGaussFeature.class, sigma);
	}

	public FeatureOp sobelGradient(double sigma) {
		return createFeature(SingleSobelGradientFeature.class, sigma);
	}

	public FeatureOp gradient(double sigma) {
		return createFeature(SingleGradientFeature.class, sigma);
	}

	public FeatureOp lipschitz(double slope, long border) {
		return createFeature(SingleLipschitzFeature.class, slope, border);
	}

	public FeatureOp hessian(double sigma) {
		return createFeature(SingleHessianFeature.class, sigma);
	}

	public FeatureOp differenceOfGaussians(double sigma1, double sigma2) {
		return createFeature(SingleDifferenceOfGaussiansFeature.class, sigma1, sigma2);
	}

	private FeatureOp createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return Features.create(ops, aClass, globalSettings, args);
	}
}
