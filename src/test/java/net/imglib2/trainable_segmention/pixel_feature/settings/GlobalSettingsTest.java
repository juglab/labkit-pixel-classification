package net.imglib2.trainable_segmention.pixel_feature.settings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GlobalSettingsTest {

	@Test
	public void testHashCode() {
		GlobalSettings globalsA = GlobalSettings.default2d().channels(ChannelSetting.RGB).radiiRange(3.0, 16.0).membraneThickness(2.5).build();
		GlobalSettings globalsB = GlobalSettings.default2d().channels(ChannelSetting.RGB).radiiRange(3.0, 16.0).membraneThickness(2.5).build();
		assertEquals(globalsA.hashCode(), globalsB.hashCode());
	}
}
