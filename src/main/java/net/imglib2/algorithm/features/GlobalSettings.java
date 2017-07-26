package net.imglib2.algorithm.features;

/**
 * @author Matthias Arzt
 */
public class GlobalSettings {

	private final double minSigma;

	private final double maxSigma;

	private final double membraneThickness;

	public GlobalSettings(double minSigma, double maxSigma, double membraneThickness) {
		this.minSigma = minSigma;
		this.maxSigma = maxSigma;
		this.membraneThickness = membraneThickness;
	}

	public double minSigma() {
		return minSigma;
	}

	public double maxSigma() {
		return maxSigma;
	}

	public static GlobalSettings defaultSettings() {
		return new GlobalSettings(1.0, 16.0, 1.0);
	}
}
