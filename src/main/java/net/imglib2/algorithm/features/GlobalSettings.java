package net.imglib2.algorithm.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Matthias Arzt
 */
public final class GlobalSettings {

	public static GlobalSettings defaultSettings() {
		return new GlobalSettings(1.0, 16.0, 1.0);
	}

	private final List<Double> sigmas;

	private final double membraneThickness;

	public GlobalSettings(List<Double> sigmas, double membraneThickness) {
		this.sigmas = Collections.unmodifiableList(new ArrayList<>(sigmas));
		this.membraneThickness = membraneThickness;
	}

	public GlobalSettings(double minSigma, double maxSigma, double membraneThickness) {
		this(initSigmas(minSigma, maxSigma), membraneThickness);
	}

	private static List<Double> initSigmas(double minSigma, double maxSigma) {
		List<Double> sigmas = new ArrayList<>();
		for(double sigma = minSigma; sigma <= maxSigma; sigma *= 2.0)
			sigmas.add(sigma);
		return sigmas;
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.sigmas(), globalSettings.membraneThickness());
	}

	public List<Double> sigmas() {
		return sigmas;
	}

	public double membraneThickness() {
		return membraneThickness;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sigmas, membraneThickness);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GlobalSettings))
			return false;
		GlobalSettings settings = (GlobalSettings) obj;
		return sigmas.equals(settings.sigmas) &&
				membraneThickness() == settings.membraneThickness();
	}

}
