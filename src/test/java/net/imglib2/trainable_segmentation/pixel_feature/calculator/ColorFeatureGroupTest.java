
package net.imglib2.trainable_segmentation.pixel_feature.calculator;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.trainable_segmentation.RevampUtils;
import net.imglib2.trainable_segmentation.Utils;
import net.imglib2.trainable_segmentation.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmentation.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class ColorFeatureGroupTest {

	private final FeatureCalculator colorGroup = FeatureCalculator.default2d()
		.channels(ChannelSetting.RGB)
		.addFeature(SingleFeatures.gauss(8.0))
		.build();

	private final FeatureCalculator grayGroup = FeatureCalculator.default2d()
		.channels(ChannelSetting.SINGLE)
		.addFeature(SingleFeatures.gauss(8.0))
		.build();

	private final Img<ARGBType> image = Utils.loadImageARGBType(
		"https://imagej.nih.gov/ij/images/clown.png");

	@Test
	public void testAttributes() {
		List<String> expected = Arrays.asList(
			"red_gaussian blur sigma=8.0",
			"green_gaussian blur sigma=8.0",
			"blue_gaussian blur sigma=8.0");
		assertEquals(expected, colorGroup.attributeLabels());
	}

	@Test
	public void testImage() {
		RandomAccessibleInterval<FloatType> result = colorGroup.apply(image);

		List<RandomAccessibleInterval<FloatType>> results = RevampUtils.slices(result);
		List<RandomAccessibleInterval<FloatType>> channels = RevampUtils.splitChannels(image);
		for (int i = 0; i < 3; i++) {
			RandomAccessibleInterval<FloatType> expected = RevampUtils.slices(grayGroup.apply(channels
				.get(i))).get(0);
			Utils.assertImagesEqual(35, expected, results.get(i));
		}
	}

	@Test
	public void testSplittedColorImage() {
		RandomAccessibleInterval<FloatType> asFloat = Views.stack(RevampUtils.splitChannels(image));
		RandomAccessibleInterval<FloatType> expected = colorGroup.apply(asFloat);
		RandomAccessibleInterval<FloatType> actual = colorGroup.apply(image);
		Utils.assertImagesEqual(actual, expected);
	}
}
