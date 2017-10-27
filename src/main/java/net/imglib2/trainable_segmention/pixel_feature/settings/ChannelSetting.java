package net.imglib2.trainable_segmention.pixel_feature.settings;

import java.util.Arrays;
import java.util.List;

public enum ChannelSetting {
	RGB("red", "green", "blue"),
	SINGLE("");

	private final List<String> channels;

	ChannelSetting(String... names) {
		channels = Arrays.asList(names);
	}

	public List<String> channels() {
		return channels;
	}
}
