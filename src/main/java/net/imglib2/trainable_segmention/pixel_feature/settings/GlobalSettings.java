
package net.imglib2.trainable_segmention.pixel_feature.settings;

import net.imglib2.util.Cast;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public final class GlobalSettings {

	private final ChannelSetting channelSetting;

	private final int numDimensions;

	private final List<Double> sigmas;

	private final List<Double> pixelSize;

	private GlobalSettings(ChannelSetting channelSetting, int numDimensions, List<Double> sigmas,
		List<Double> pixelSize)
	{
		this.channelSetting = channelSetting;
		this.numDimensions = numDimensions;
		this.sigmas = Collections.unmodifiableList(new ArrayList<>(sigmas));
		this.pixelSize = Collections.unmodifiableList(pixelSize == null ? ones(numDimensions)
			: new ArrayList<>(pixelSize));
	}

	public GlobalSettings(GlobalSettings globalSettings) {
		this(globalSettings.channelSetting, globalSettings.numDimensions, globalSettings.sigmas,
			globalSettings.pixelSize);
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

	public List<Double> pixelSize() {
		return pixelSize;
	}

	public double[] pixelSizeAsDoubleArray() {
		return pixelSize.stream().mapToDouble(x -> x).toArray();
	}

	private List<Double> ones(int numDimensions) {
		return IntStream.range(0, numDimensions).mapToObj(ignore -> 1.0).collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		return Objects.hash(channelSetting, numDimensions, sigmas, pixelSize());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GlobalSettings))
			return false;
		GlobalSettings settings = (GlobalSettings) obj;
		return channelSetting.equals(settings.channelSetting) &&
			numDimensions == settings.numDimensions &&
			sigmas.equals(settings.sigmas) &&
			pixelSize().equals(settings.pixelSize());
	}

	public static class AbstractBuilder<T> {

		private ChannelSetting channelSetting = ChannelSetting.SINGLE;
		private int numDimensions = 3;
		private List<Double> sigmas = Arrays.asList(1.0, 2.0, 4.0, 8.0);
		private List<Double> pixelSize = null;

		protected AbstractBuilder() {

		}

		public T channels(ChannelSetting channelSetting) {
			this.channelSetting = channelSetting;
			return Cast.unchecked(this);
		}

		public T dimensions(int numDimensions) {
			this.numDimensions = numDimensions;
			return Cast.unchecked(this);
		}

		public T sigmas(List<Double> sigmas) {
			this.sigmas = sigmas;
			return Cast.unchecked(this);
		}

		public T sigmas(double... sigmas) {
			return sigmas(DoubleStream.of(sigmas).boxed().collect(Collectors.toList()));
		}

		public T pixelSize(List<Double> pixelSize) {
			this.pixelSize = pixelSize;
			return Cast.unchecked(this);
		}

		public T pixelSize(double... pixelSize) {
			return pixelSize(DoubleStream.of(pixelSize).boxed().collect(Collectors.toList()));
		}

		public T sigmaRange(double minSigma, double maxSigma) {
			List<Double> sigmas = new ArrayList<>();
			for (double sigma = minSigma; sigma <= maxSigma; sigma *= 2.0)
				sigmas.add(sigma);
			sigmas(sigmas);
			return Cast.unchecked(this);
		}

		protected GlobalSettings buildGlobalSettings() {
			return new GlobalSettings(channelSetting, numDimensions, sigmas, pixelSize);
		}
	}

	public static class Builder extends AbstractBuilder<Builder> {

		private Builder() {

		}

		public GlobalSettings build() {
			return buildGlobalSettings();
		}

	}
}
