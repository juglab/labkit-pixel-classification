/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

import com.google.gson.Gson;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ChannelSettingTest {

	@Test
	public void testIsMultiple() {
		assertFalse(ChannelSetting.SINGLE.isMultiple());
		assertTrue(ChannelSetting.multiple(42).isMultiple());
	}

	@Test
	public void testChannels() {
		assertEquals(Arrays.asList("channel1", "channel2"), ChannelSetting.multiple(2).channels());
	}

	@Test
	public void testToString() {
		testToString("SINGLE", ChannelSetting.SINGLE);
		testToString("RGB8", ChannelSetting.RGB);
		testToString("RGB", ChannelSetting.DEPRECATED_RGB);
		testToString("MULTIPLE_42", ChannelSetting.multiple(42));
	}

	private void testToString(String expected, ChannelSetting channelSetting) {
		assertEquals(expected, channelSetting.toString());
	}

	@Test
	public void testEquals() {
		assertEquals(ChannelSetting.SINGLE, ChannelSetting.SINGLE);
		assertEquals(ChannelSetting.RGB, ChannelSetting.RGB);
		assertEquals(ChannelSetting.multiple(42), ChannelSetting.multiple(42));
	}

	@Test
	public void testNoEquals() {
		assertNotEquals(ChannelSetting.SINGLE, ChannelSetting.RGB);
		assertNotEquals(ChannelSetting.SINGLE, ChannelSetting.multiple(32));
		assertNotEquals(ChannelSetting.multiple(32), ChannelSetting.multiple(42));
	}

	@Test
	public void testValueOf() {
		testValueOf(ChannelSetting.RGB);
		testValueOf(ChannelSetting.DEPRECATED_RGB);
		testValueOf(ChannelSetting.SINGLE);
		testValueOf(ChannelSetting.multiple(42));
	}

	private void testValueOf(ChannelSetting channelSetting) {
		String asString = channelSetting.toString();
		ChannelSetting result = ChannelSetting.valueOf(asString);
		assertEquals(channelSetting, result);
	}

	@Test
	public void testToJson() {
		String result = new Gson().toJson(ChannelSetting.multiple(42));
		assertEquals("\"MULTIPLE_42\"", result);
	}

	@Test
	public void testFromJson() {
		ChannelSetting result = new Gson().fromJson("\"RGB8\"", ChannelSetting.class);
		assertEquals(ChannelSetting.RGB, result);
	}
}
