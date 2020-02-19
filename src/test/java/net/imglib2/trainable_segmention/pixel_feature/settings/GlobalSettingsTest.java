
package net.imglib2.trainable_segmention.pixel_feature.settings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GlobalSettingsTest {

	@Test
	public void testHashCode() {
		GlobalSettings globalsA = new GlobalSettings(ChannelSetting.RGB, 2, 3.0, 16.0, 2.5);
		GlobalSettings globalsB = new GlobalSettings(ChannelSetting.RGB, 2, 3.0, 16.0, 2.5);
		assertEquals(globalsA.hashCode(), globalsB.hashCode());
	}
}
