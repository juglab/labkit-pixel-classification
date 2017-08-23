package net.imglib2.algorithm.features;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public final class GlobalSettings {

	public static GlobalSettings defaultSettings() {
		return new GlobalSettings(ImageType.GRAY_SCALE, 1.0, 16.0, 1.0);
	}

	private final ImageType imageType;

	private final List<Double> sigmas;

	private final double membraneThickness;

	public GlobalSettings(ImageType imageType, List<Double> sigmas, double membraneThickness) {
		this.imageType = imageType;
		this.sigmas = Collections.unmodifiableList(new ArrayList<>(sigmas));
		this.membraneThickness = membraneThickness;
	}

	public GlobalSettings(ImageType imageType, double minSigma, double maxSigma, double membraneThickness) {
		this(imageType, initSigmas(minSigma, maxSigma), membraneThickness);
	}

	private static List<Double> initSigmas(double minSigma, double maxSigma) {
		List<Double> sigmas = new ArrayList<>();
		for(double sigma = minSigma; sigma <= maxSigma; sigma *= 2.0)
			sigmas.add(sigma);
		return sigmas;
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.imageType(), globalSettings.sigmas(), globalSettings.membraneThickness());
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
		return imageType.equals(settings.imageType) &&
				sigmas.equals(settings.sigmas) &&
				membraneThickness == settings.membraneThickness;
	}

	public ImageType imageType() {
		return imageType;
	}

	public enum ImageType {
		COLOR("red", "green", "blue"), GRAY_SCALE("");

		private int channelCount;

		private List<String> channelNames;

		ImageType(String... names) {
			channelCount = names.length;
			channelNames = Arrays.asList(names);
		}

		public int channelCount() {
			return channelCount;
		}

		public List<String> channelNames() {
			return channelNames;
		}
	}
}
