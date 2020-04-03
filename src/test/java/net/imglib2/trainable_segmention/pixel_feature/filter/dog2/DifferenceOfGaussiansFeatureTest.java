
package net.imglib2.trainable_segmention.pixel_feature.filter.dog2;

import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DifferenceOfGaussiansFeatureTest {

	public DifferenceOfGaussiansFeatureTest(boolean useGpu) {
		this.useGpu = useGpu;
	}

	@Parameterized.Parameters(name = "useGpu = {0}")
	public static List<Boolean> data() {
		return Arrays.asList(false, true);
	}

	private final boolean useGpu;

	@Test
	public void testAtributes() {
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.sigmas(1.0, 2.0, 3.0)
			.addFeature(GroupedFeatures.differenceOfGaussians())
			.build();
		calculator.setUseGpu(useGpu);
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Arrays.asList(
			"difference of gaussians sigma1=1.0 sigma2=2.0",
			"difference of gaussians sigma1=1.0 sigma2=3.0",
			"difference of gaussians sigma1=2.0 sigma2=3.0");
		assertEquals(expected, attributeLabels);
	}
}
