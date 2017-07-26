package net.imglib2.algorithm.features;

import java.util.Objects;

/**
 * @author Matthias Arzt
 */
public final class GlobalSettings {

	public static GlobalSettings defaultSettings() {
		return new GlobalSettings(1.0, 16.0, 1.0);
	}

	private final double minSigma;

	private final double maxSigma;

	private final double membraneThickness;

	public GlobalSettings(double minSigma, double maxSigma, double membraneThickness) {
		this.minSigma = minSigma;
		this.maxSigma = maxSigma;
		this.membraneThickness = membraneThickness;
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.minSigma(), globalSettings.maxSigma(), globalSettings.membraneThickness());
	}

	public double minSigma() {
		return minSigma;
	}

	public double maxSigma() {
		return maxSigma;
	}

	public double membraneThickness() {
		return membraneThickness;
	}

	@Override
	public int hashCode() {
		return Objects.hash(minSigma, maxSigma, membraneThickness);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GlobalSettings))
			return false;
		GlobalSettings settings = (GlobalSettings) obj;
		return minSigma() == settings.minSigma() &&
				maxSigma() == settings.maxSigma() &&
				membraneThickness() == settings.membraneThickness();
	}

}
