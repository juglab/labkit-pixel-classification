package net.imglib2.trainable_segmention.pixel_feature.settings;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public final class GlobalSettings {

	private final ChannelSetting channelSetting;

	private final int numDimensions;

	private final List<Double> radii;

	private final double scaleFactor;

	private final double membraneThickness;
	private List<Double> sigmas;

	private GlobalSettings(ChannelSetting channelSetting, int numDimensions, List<Double> radii, double scaleFactor, double membraneThickness) {
		this.channelSetting = channelSetting;
		this.numDimensions = numDimensions;
		this.radii = Collections.unmodifiableList(new ArrayList<>(radii));
		this.membraneThickness = membraneThickness;
		this.scaleFactor = scaleFactor;
		this.sigmas = Collections.unmodifiableList(this.radii.stream().map(x -> x * scaleFactor).collect(Collectors.toList()));
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.channelSetting(), globalSettings.numDimensions(), globalSettings.radii(), globalSettings.scaleFactor(), globalSettings.membraneThickness());
	}

	public static Builder default2d() {
		return new Builder()
				.channels(ChannelSetting.SINGLE)
				.dimensions(2)
				.radiiRange(1.0, 16.0)
				.scaleFactor(0.4)
				.membraneThickness(1.0);
	}

	public static Builder default3d() {
		return new Builder()
				.channels(ChannelSetting.SINGLE)
				.dimensions(3)
				.radiiRange(1.0, 8.0)
				.membraneThickness(1.0);
	}

	public ChannelSetting channelSetting() {
		return channelSetting;
	}

	public int numDimensions() {
		return numDimensions;
	}

	public List<Double> radii() {
		return radii;
	}

	public List<Double> sigmas() {
		return sigmas;
	}

	private double scaleFactor() {
		return scaleFactor;
	}

	public double membraneThickness() {
		return membraneThickness;
	}

	@Override
	public int hashCode() {
		return Objects.hash(radii, numDimensions, membraneThickness, channelSetting);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GlobalSettings))
			return false;
		GlobalSettings settings = (GlobalSettings) obj;
		return channelSetting.equals(settings.channelSetting) &&
				radii.equals(settings.radii) &&
				membraneThickness == settings.membraneThickness;
	}

	public static class Builder {

		private ChannelSetting channelSetting = ChannelSetting.SINGLE;
		private int numDimensions = 3;
		private List<Double> radii = Arrays.asList(1.0, 2.0, 4.0, 8.0);
		private double membraneThickness = 1;
		private double scaleFactor = 1;

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

		public Builder radii(List<Double> radii) {
			this.radii = radii;
			return this;
		}

		public Builder radii(Double... radii) {
			return radii(Arrays.asList(radii));
		}

		public Builder radiiRange(double minRadius, double maxRadius) {
			List<Double> radii = new ArrayList<>();
			for(double radius = minRadius; radius <= maxRadius; radius *= 2.0)
				radii.add(radius);
			radii(radii);
			return this;
		}

		public Builder scaleFactor(double scaleFactor) {
			this.scaleFactor = scaleFactor;
			return this;
		}

		public Builder membraneThickness(double membraneThickness) {
			this.membraneThickness = membraneThickness;
			return this;
		}

		public GlobalSettings build() {
			return new GlobalSettings(channelSetting, numDimensions, radii, scaleFactor, membraneThickness);
		}
	}
}
