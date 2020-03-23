
package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SingleStatisticsFeatureTest {

	FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleFeatures.statistics(1))
		.build();

	@Test
	public void testApply() {
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			0, 0, 0, 0,
			0, 1, -1, 0,
			0, 0, 0, 0
		}, 4, 3);
		Img<FloatType> output = ArrayImgs.floats(4, 3, 4);
		Img<FloatType> expectedMin = ArrayImgs.floats(new float[] {
			0, -1, -1, -1,
			0, -1, -1, -1,
			0, -1, -1, -1
		}, 4, 3);
		Img<FloatType> expectedMax = ArrayImgs.floats(new float[] {
			1, 1, 1, 0,
			1, 1, 1, 0,
			1, 1, 1, 0
		}, 4, 3);
		Img<FloatType> expectedMean = ArrayImgs.floats(new float[] {
			1 / 9f, 0, 0, -1 / 9f,
			1 / 9f, 0, 0, -1 / 9f,
			1 / 9f, 0, 0, -1 / 9f
		}, 4, 3);
		Img<FloatType> expectedVariance = ArrayImgs.floats(new float[] {
			1 / 9f, 2 / 8f, 2 / 8f, 1 / 9f,
			1 / 9f, 2 / 8f, 2 / 8f, 1 / 9f,
			1 / 9f, 2 / 8f, 2 / 8f, 1 / 9f
		}, 4, 3);
		calculator.apply(Views.extendBorder(input), output);
		ImgLib2Assert.assertImageEquals(Views.stack(expectedMin, expectedMax, expectedMean,
			expectedVariance),
			output);
	}

	@Test
	public void testAttributeLabels() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Arrays.asList(
			"min filter radius=1.0",
			"max filter radius=1.0",
			"mean filter radius=1.0",
			"variance filter radius=1.0");
		assertEquals(expected, attributeLabels);
	}

	@Test
	public void testRadius0() {
		OpEnvironment ops = new Context().service(OpService.class);
		FeatureSetting featureSetting = SingleFeatures.statistics(0);
		FeatureOp feature = featureSetting.newInstance(ops, GlobalSettings.default2d().build());
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			0, 0, 0, 0,
			0, 1, -1, 0,
			0, 0, 0, 0
		}, 4, 3);
		Img<FloatType> output = ArrayImgs.floats(4, 3, 4);
		Img<FloatType> expectedMin = input;
		Img<FloatType> expectedMax = input;
		Img<FloatType> expectedMean = input;
		Img<FloatType> expectedVariance = ArrayImgs.floats(4, 3);
		feature.apply(input, RevampUtils.slices(output));
		ImgLib2Assert.assertImageEquals(Views.stack(expectedMin, expectedMax, expectedMean,
			expectedVariance),
			output);
	}

	@Test
	public void testAnisotropic() {
		OpEnvironment ops = new Context().service(OpService.class);
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.pixelSize(1, 2)
			.addFeature(SingleFeatures.max(1))
			.build();
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		Img<FloatType> output = ArrayImgs.floats(5, 5, 1);
		Img<FloatType> expectedMax = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 1, 1, 1, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		calculator.apply(input, output);
		ImgLib2Assert.assertImageEquals(Views.stack(expectedMax), output);

	}
}
