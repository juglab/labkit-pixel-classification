package net.imglib2.algorithm.features;

import net.imagej.ops.OpEnvironment;
import net.imglib2.algorithm.features.gson.FeaturesGson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Matthias Arzt
 */
public class FeatureGroupTest {

	@Test
	public void testEquals() {
		OpEnvironment ops = Utils.ops();
		GlobalSettings defaultSettings = GlobalSettings.defaultSettings();
		FeatureGroup featureGroup = Features.group(ops, defaultSettings, SingleFeatures.gauss(3.0));
		FeatureGroup equalFeatureGroup = Features.group(ops, defaultSettings, SingleFeatures.gauss(3.0));
		FeatureGroup differentFeatureGroup = Features.group(ops, defaultSettings, SingleFeatures.gauss(4.0));
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.COLOR, 1.0, 16.0, 1.0);
		FeatureGroup differentFeatureGroup2 = Features.group(ops, settings, SingleFeatures.gauss(3.0));
		assertEquals(featureGroup, equalFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup2);
	}

	@Test
	public void testSerialization() {
		testSerialization(Features.group(Utils.ops(), GlobalSettings.defaultSettings(), SingleFeatures.gauss(1.0)));
	}

	@Test
	public void testColoredSerialization() {
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.COLOR, 1.0, 16.0, 1.0);
		testSerialization(Features.group(Utils.ops(), settings, SingleFeatures.hessian(3.0)));
	}

	private void testSerialization(FeatureGroup featureGroup) {
		String json = FeaturesGson.toJson(featureGroup);
		FeatureGroup object2 = FeaturesGson.fromJson(json, Utils.ops());
		assertEquals(featureGroup, object2);
	}

}
