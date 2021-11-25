
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
