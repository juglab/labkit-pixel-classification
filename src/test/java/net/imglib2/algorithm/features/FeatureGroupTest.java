package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.gson.FeaturesGson;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleGaussFeature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Matthias Arzt
 */
public class FeatureGroupTest {

	@Test
	public void testEqualFeatureSettings() {
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.GRAY_SCALE, 2.0, 16.0, 1.0);
		GlobalSettings equalSettings = new GlobalSettings(GlobalSettings.ImageType.GRAY_SCALE, 2.0, 16.0, 1.0);
		Features.grayGroup(gaussFeature(settings), gaussFeature(equalSettings));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDifferentFeatureSettings() {
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.GRAY_SCALE, 2.0, 16.0, 1.0);
		GlobalSettings differentSettings = new GlobalSettings(GlobalSettings.ImageType.GRAY_SCALE, 3.0, 16.0, 1.0);
		Features.grayGroup(gaussFeature(settings), gaussFeature(differentSettings));
	}

	private FeatureOp gaussFeature(GlobalSettings settingsA) {
		return Features.create(SingleGaussFeature.class, settingsA,1.0);
	}

	@Test
	public void testEquals() {
		FeatureGroup featureGroup = Features.group(SingleFeatures.SingleFeatures.gauss(3.0));
		FeatureGroup equalFeatureGroup = Features.group(SingleFeatures.SingleFeatures.gauss(3.0));
		FeatureGroup differentFeatureGroup = Features.group(SingleFeatures.SingleFeatures.gauss(4.0));
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.COLOR, 1.0, 16.0, 1.0);
		FeatureGroup differentFeatureGroup2 = Features.group(new SingleFeatures(settings).gauss(3.0));
		assertEquals(featureGroup, equalFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup);
		assertNotEquals(featureGroup, differentFeatureGroup2);
	}

	@Test
	public void testSerialization() {
		testSerialization(Features.group(SingleFeatures.SingleFeatures.gauss(1.0)));
	}

	@Test
	public void testColoredSerialization() {
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.COLOR, 1.0, 16.0, 1.0);
		testSerialization(Features.group(new SingleFeatures(settings).hessian(3.0)));
	}

	private void testSerialization(FeatureGroup featureGroup) {
		String json = FeaturesGson.toJson(featureGroup);
		FeatureGroup object2 = FeaturesGson.fromJson(json);
		assertEquals(featureGroup, object2);
	}

}
