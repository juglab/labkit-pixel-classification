package net.imglib2.trainable_segmention;

import com.google.gson.JsonElement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingsTest {

	@Test
	public void testEquals() {
		GlobalSettings defaultSettings = GlobalSettings.defaultSettings();
		FeatureSettings featureGroup = new FeatureSettings(defaultSettings, SingleFeatures.gauss(3.0));
		FeatureSettings equalFeatureGroup = new FeatureSettings(defaultSettings, SingleFeatures.gauss(3.0));
		FeatureSettings differentFeatureGroup = new FeatureSettings(defaultSettings, SingleFeatures.gauss(4.0));
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.COLOR, 1.0, 16.0, 1.0);
		FeatureSettings differentFeatureGroup2 = new FeatureSettings(settings, SingleFeatures.gauss(3.0));
		assertEquals(featureGroup, equalFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup2);
	}

	@Test
	public void testSerialization() {
		testSerialization(new FeatureSettings(GlobalSettings.defaultSettings(), SingleFeatures.gauss(1.0)));
	}

	@Test
	public void testColoredSerialization() {
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.COLOR, 1.0, 16.0, 1.0);
		testSerialization(new FeatureSettings(settings, SingleFeatures.hessian(3.0)));
	}

	private void testSerialization(FeatureSettings fs) {
		JsonElement json = fs.toJson();
		FeatureSettings object2 = FeatureSettings.fromJson(json);
		assertEquals(fs, object2);
	}

}
