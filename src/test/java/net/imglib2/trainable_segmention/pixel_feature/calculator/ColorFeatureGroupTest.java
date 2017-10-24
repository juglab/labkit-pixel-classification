package net.imglib2.trainable_segmention.pixel_feature.calculator;

import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class ColorFeatureGroupTest {

	private final GlobalSettings colorSettings = new GlobalSettings(GlobalSettings.ImageType.COLOR,
			Arrays.asList(8.0), 1.0);

	private final GlobalSettings graySettings = new GlobalSettings(GlobalSettings.ImageType.GRAY_SCALE,
			colorSettings.sigmas(), colorSettings.membraneThickness());

	private final FeatureGroup colorGroup = new AbstractFeatureGroup(Utils.ops(),
			new FeatureSettings(colorSettings, GroupedFeatures.gauss()));

	private final FeatureGroup grayGroup = new AbstractFeatureGroup(Utils.ops(),
			new FeatureSettings(graySettings, GroupedFeatures.gauss()));

	private final Img<ARGBType> image = ImageJFunctions.wrapRGBA(new ImagePlus("https://imagej.nih.gov/ij/images/clown.png"));

	@Test
	public void testAttributes() {
		assertEquals(Arrays.asList("red_Gaussian_blur_8.0", "green_Gaussian_blur_8.0", "blue_Gaussian_blur_8.0"), colorGroup.attributeLabels());
	}

	@Test
	public void testImage() {
		RandomAccessibleInterval<FloatType> result = Features.applyOnImg(colorGroup, image);

		List<RandomAccessibleInterval<FloatType>> results = RevampUtils.slices(result);
		List<RandomAccessibleInterval<FloatType>> channels = RevampUtils.splitChannels(image);
		for (int i = 0; i < 3; i++) {
			RandomAccessibleInterval<FloatType> expected = RevampUtils.slices(Features.applyOnImg(grayGroup, channels.get(i))).get(0);
			Utils.assertImagesEqual(35, expected, results.get(i));
		}
	}
}
