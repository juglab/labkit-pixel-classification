
package net.imglib2.trainable_segmention.pixel_feature.filter.gauss;

import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GaussianBlurFeatureTest {

	private final FeatureCalculator calculator = FeatureCalculator.default2d().sigmas(1.0, 2.0)
		.addFeature(GroupedFeatures.gauss()).build();

	@Test
	public void testAttributeLabels() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Arrays.asList(
			"gaussian blur sigma=1.0",
			"gaussian blur sigma=2.0");
		assertEquals(expected, attributeLabels);
	}
}
