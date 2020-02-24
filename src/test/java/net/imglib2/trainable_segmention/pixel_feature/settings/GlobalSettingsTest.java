
package net.imglib2.trainable_segmention.pixel_feature.settings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GlobalSettingsTest {

	@Test
	public void testHashCode() {
		GlobalSettings globalsA = GlobalSettings.default2d()
			.channels(ChannelSetting.RGB)
			.dimensions(2)
			.sigmaRange(3.0, 16.0)
			.pixelSize(2.0, 3.0)
			.build();
		GlobalSettings globalsB = GlobalSettings.default2d()
			.channels(ChannelSetting.RGB)
			.dimensions(2)
			.sigmaRange(3.0, 16.0)
			.pixelSize(2.0, 3.0)
			.build();
		assertEquals(globalsA.hashCode(), globalsB.hashCode());
	}
}
