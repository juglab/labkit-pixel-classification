
package net.imglib2.trainable_segmentation.pixel_feature.settings;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@JsonAdapter(ChannelSettingJsonAdapter.class)
public class ChannelSetting {

	public static final ChannelSetting DEPRECATED_RGB = new ChannelSetting("RGB", false, "red", "green", "blue");
	public static final ChannelSetting RGB = new ChannelSetting("RGB8", false, "red", "green", "blue");
	public static final ChannelSetting SINGLE = new ChannelSetting("SINGLE", false, "");

	public static ChannelSetting multiple(int n) {
		return new ChannelSetting("MULTIPLE_" + n, true,
			IntStream.range(0, n).mapToObj(i -> "channel" + (i + 1)).toArray(String[]::new));
	}

	private final List<String> channels;
	private final String toString;
	private final boolean isMultiple;

	private ChannelSetting(String toString, boolean isMultiple, String... names) {
		this.channels = Collections.unmodifiableList(Arrays.asList(names));
		this.toString = toString;
		this.isMultiple = isMultiple;
	}

	public List<String> channels() {
		return channels;
	}

	public boolean isMultiple() {
		return isMultiple;
	}

	public static ChannelSetting valueOf(String value) {
		if (value.equals(DEPRECATED_RGB.toString()))
			return DEPRECATED_RGB;
		if (value.equals(RGB.toString()))
			return RGB;
		if (value.equals(SINGLE.toString()))
			return SINGLE;
		if (value.startsWith("MULTIPLE_")) {
			String number = value.substring("MULTIPLE_".length());
			try {
				return multiple(Integer.valueOf(number));
			}
			catch (NumberFormatException ignore) {
				throw new IllegalArgumentException();
			}
		}
		throw new IllegalArgumentException();
	}

	@Override
	public String toString() {
		return toString;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ChannelSetting && toString.equals(o.toString());
	}
}

class ChannelSettingJsonAdapter extends TypeAdapter<ChannelSetting> {

	@Override
	public void write(JsonWriter out, ChannelSetting value)
		throws IOException
	{
		out.value(value.toString());
	}

	@Override
	public ChannelSetting read(JsonReader in) throws IOException {
		return ChannelSetting.valueOf(in.nextString());
	}
}
