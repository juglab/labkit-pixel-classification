package net.imglib2.trainable_segmention.pixel_feature.settings;

import java.util.*;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public final class GlobalSettings {

	private final ChannelSetting channelSetting;

	private final int numDimensions;

	private final List<Double> sigmas;

	private final double membraneThickness;

	private GlobalSettings(ChannelSetting channelSetting, int numDimensions, List<Double> sigmas, double membraneThickness) {
		this.channelSetting = channelSetting;
		this.numDimensions = numDimensions;
		this.sigmas = Collections.unmodifiableList(new ArrayList<>(sigmas));
		this.membraneThickness = membraneThickness;
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.channelSetting(), globalSettings.numDimensions(), globalSettings.sigmas(), globalSettings.membraneThickness());
	}

	public static Builder default2d() {
		return new Builder()
				.channels(ChannelSetting.SINGLE)
				.dimensions(2)
				.sigmaRange(1.0, 16.0)
				.membraneThickness(1.0);
	}

	public static Builder default3d() {
		return new Builder()
				.channels(ChannelSetting.SINGLE)
				.dimensions(3)
				.sigmaRange(1.0, 8.0)
				.membraneThickness(1.0);
	}

	public ChannelSetting channelSetting() {
		return channelSetting;
	}

	public int numDimensions() {
		return numDimensions;
	}

	public List<Double> sigmas() {
		return sigmas;
	}

	public double membraneThickness() {
		return membraneThickness;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sigmas, numDimensions, membraneThickness, channelSetting);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GlobalSettings))
			return false;
		GlobalSettings settings = (GlobalSettings) obj;
		return channelSetting.equals(settings.channelSetting) &&
				sigmas.equals(settings.sigmas) &&
				membraneThickness == settings.membraneThickness;
	}

	public static class Builder {

		private ChannelSetting channelSetting = ChannelSetting.SINGLE;
		private int numDimensions = 3;
		private List<Double> sigmas = Arrays.asList(1.0, 2.0, 4.0, 8.0);
		private double membraneThickness = 1;

		private Builder() {

		}

		public Builder channels(ChannelSetting channelSetting) {
			this.channelSetting = channelSetting;
			return this;
		}

		public Builder dimensions(int numDimensions) {
			this.numDimensions = numDimensions;
			return this;
		}

		public Builder sigmas(List<Double> sigmas) {
			this.sigmas = sigmas;
			return this;
		}

		public Builder sigmas(Double... sigmas) {
			return sigmas(Arrays.asList(sigmas));
		}

		public Builder membraneThickness(double membraneThickness) {
			this.membraneThickness = membraneThickness;
			return this;
		}

		public Builder sigmaRange(double minSigma, double maxSigma) {
			List<Double> sigmas = new ArrayList<>();
			for(double sigma = minSigma; sigma <= maxSigma; sigma *= 2.0)
				sigmas.add(sigma);
			sigmas(sigmas);
			return this;
		}

		public GlobalSettings build() {
			return new GlobalSettings(channelSetting, numDimensions, sigmas, membraneThickness);
		}
	}
}
