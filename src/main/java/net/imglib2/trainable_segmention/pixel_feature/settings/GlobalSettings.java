
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

	private GlobalSettings(ChannelSetting channelSetting, int numDimensions, List<Double> sigmas) {
		this.channelSetting = channelSetting;
		this.numDimensions = numDimensions;
		this.sigmas = Collections.unmodifiableList(new ArrayList<>(sigmas));
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.channelSetting(), globalSettings.numDimensions(), globalSettings.sigmas());
	}

	public static Builder default2d() {
		return new Builder()
			.channels(ChannelSetting.SINGLE)
			.dimensions(2)
			.sigmaRange(1.0, 16.0);
	}

	public static Builder default3d() {
		return new Builder()
			.channels(ChannelSetting.SINGLE)
			.dimensions(3)
			.sigmaRange(1.0, 8.0);
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

	@Override
	public int hashCode() {
		return Objects.hash(channelSetting, numDimensions, sigmas);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GlobalSettings))
			return false;
		GlobalSettings settings = (GlobalSettings) obj;
		return channelSetting.equals(settings.channelSetting) &&
			numDimensions == settings.numDimensions &&
			sigmas.equals(settings.sigmas);
	}

	public static class Builder {

		private ChannelSetting channelSetting = ChannelSetting.SINGLE;
		private int numDimensions = 3;
		private List<Double> sigmas = Arrays.asList(1.0, 2.0, 4.0, 8.0);

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

		public Builder sigmas(List<Double> radii) {
			this.sigmas = radii;
			return this;
		}

		public Builder sigmas(Double... radii) {
			return sigmas(Arrays.asList(radii));
		}

		public Builder sigmaRange(double minSigma, double maxSigma) {
			List<Double> sigmas = new ArrayList<>();
			for (double sigma = minSigma; sigma <= maxSigma; sigma *= 2.0)
				sigmas.add(sigma);
			sigmas(sigmas);
			return this;
		}

		public GlobalSettings build() {
			return new GlobalSettings(channelSetting, numDimensions, sigmas);
		}

	}
}
