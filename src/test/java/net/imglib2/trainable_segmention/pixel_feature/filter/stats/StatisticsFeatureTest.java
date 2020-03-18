
package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link StatisticsFeature}.
 */
public class StatisticsFeatureTest {

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
			.sigmas(1., 2.)
			.addFeature(GroupedFeatures.statistics())
			.build();

	@Test
	public void test() {
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
		}, 5, 5);
		Img<FloatType> output = ArrayImgs.floats(5, 5, 8);
		// process
		calculator.apply(Views.extendBorder(input), RevampUtils.slices(output));
		// test
		Img<FloatType> expectedMin1 = filled5x5Image(0);
		Img<FloatType> expectedMax1 = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 0, 0, 0, 0,
		}, 5, 5);
		Img<FloatType> expectedMean1 = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 1 / 9f, 1 / 9f, 1 / 9f, 0,
			0, 1 / 9f, 1 / 9f, 1 / 9f, 0,
			0, 1 / 9f, 1 / 9f, 1 / 9f, 0,
			0, 0, 0, 0, 0,
		}, 5, 5);
		Img<FloatType> expectedVariance1 = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 1 / 9f, 1 / 9f, 1 / 9f, 0,
			0, 1 / 9f, 1 / 9f, 1 / 9f, 0,
			0, 1 / 9f, 1 / 9f, 1 / 9f, 0,
			0, 0, 0, 0, 0,
		}, 5, 5);
		Img<FloatType> expectedMin2 = filled5x5Image(0);
		Img<FloatType> expectedMax2 = filled5x5Image(1);
		Img<FloatType> expectedMean2 = filled5x5Image(1 / 25f);
		Img<FloatType> expectedVariance2 = filled5x5Image(1 / 25f);
		ImgLib2Assert.assertImageEqualsRealType(Views.stack(
			expectedMin1, expectedMax1, expectedMean1, expectedVariance1,
			expectedMin2, expectedMax2, expectedMean2, expectedVariance2), output, 0.0001);
	}

	private Img<FloatType> filled5x5Image(float value) {
		Img<FloatType> img = ArrayImgs.floats(5, 5);
		img.forEach(pixel -> pixel.set(value));
		return img;
	}
}
