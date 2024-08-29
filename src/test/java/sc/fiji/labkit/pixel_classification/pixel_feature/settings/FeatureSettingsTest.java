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

import com.google.gson.JsonElement;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingsTest {

	@Test
	public void testEquals() {
		GlobalSettings defaultSettings = GlobalSettings.default2d().build();
		FeatureSettings featureGroup = new FeatureSettings(defaultSettings, SingleFeatures.gauss(3.0));
		FeatureSettings equalFeatureGroup = new FeatureSettings(defaultSettings, SingleFeatures.gauss(
			3.0));
		FeatureSettings differentFeatureGroup = new FeatureSettings(defaultSettings, SingleFeatures
			.gauss(4.0));
		GlobalSettings settings = GlobalSettings.default2d()
			.channels(ChannelSetting.RGB)
			.dimensions(2)
			.sigmaRange(1.0, 16.0)
			.build();
		FeatureSettings differentFeatureGroup2 = new FeatureSettings(settings, SingleFeatures.gauss(
			3.0));
		assertEquals(featureGroup, equalFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup2);
		assertEquals(featureGroup.hashCode(), equalFeatureGroup.hashCode());
	}

	@Test
	public void testSerialization() {
		testSerialization(new FeatureSettings(GlobalSettings.default2d().build(), SingleFeatures.gauss(
			1.0)));
	}

	@Test
	public void testColoredSerialization() {
		GlobalSettings settings = GlobalSettings.default2d()
			.channels(ChannelSetting.RGB)
			.dimensions(4)
			.sigmaRange(1.0, 16.0)
			.build();
		testSerialization(new FeatureSettings(settings, SingleFeatures.hessian(3.0)));
	}

	private void testSerialization(FeatureSettings fs) {
		JsonElement json = fs.toJson();
		FeatureSettings object2 = FeatureSettings.fromJson(json);
		assertEquals(fs, object2);
	}

}
