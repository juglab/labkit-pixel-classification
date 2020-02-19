
package net.imglib2.trainable_segmention.pixel_feature.settings;

import java.util.*;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public final class GlobalSettings {

	public static GlobalSettings default2dSettings() {
		return new GlobalSettings(ChannelSetting.SINGLE, 2, 1.0, 16.0, 1.0);
	}

	public static GlobalSettings default3dSettings() {
		return new GlobalSettings(ChannelSetting.SINGLE, 3, 1.0, 8.0, 1.0);
	}

	private final ChannelSetting channelSetting;

	private final int numDimensions;

	private final List<Double> sigmas;

	private final double membraneThickness;

	public GlobalSettings(ChannelSetting channelSetting, int numDimensions, List<Double> sigmas,
		double membraneThickness)
	{
		this.channelSetting = channelSetting;
		this.numDimensions = numDimensions;
		this.sigmas = Collections.unmodifiableList(new ArrayList<>(sigmas));
		this.membraneThickness = membraneThickness;
	}

	public GlobalSettings(ChannelSetting channelSetting, int numDimensions, double minSigma,
		double maxSigma, double membraneThickness)
	{
		this(channelSetting, numDimensions, initSigmas(minSigma, maxSigma), membraneThickness);
	}

	private static List<Double> initSigmas(double minSigma, double maxSigma) {
		List<Double> sigmas = new ArrayList<>();
		for (double sigma = minSigma; sigma <= maxSigma; sigma *= 2.0)
			sigmas.add(sigma);
		return sigmas;
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.channelSetting(), globalSettings.numDimensions(), globalSettings.sigmas(),
			globalSettings.membraneThickness());
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
		if (!(obj instanceof GlobalSettings))
			return false;
		GlobalSettings settings = (GlobalSettings) obj;
		return channelSetting.equals(settings.channelSetting) &&
			sigmas.equals(settings.sigmas) &&
			membraneThickness == settings.membraneThickness;
	}
}
