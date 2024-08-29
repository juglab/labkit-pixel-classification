/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.pixel_feature.settings;

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

	public static final ChannelSetting DEPRECATED_RGB = new ChannelSetting("RGB", false,
		"red", "green", "blue");
	public static final ChannelSetting RGB = new ChannelSetting("RGB8", false,
		"red", "green", "blue");
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
