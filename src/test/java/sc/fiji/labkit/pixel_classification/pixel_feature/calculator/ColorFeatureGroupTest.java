/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.pixel_feature.calculator;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.ChannelSetting;
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
		List<RandomAccessibleInterval<FloatType>> channels = splitChannels(image);
		for (int i = 0; i < 3; i++) {
			RandomAccessibleInterval<FloatType> expected = RevampUtils.slices(grayGroup.apply(channels
				.get(i))).get(0);
			Utils.assertImagesEqual(35, expected, results.get(i));
		}
	}

	@Test
	public void testSplittedColorImage() {
		RandomAccessibleInterval<FloatType> asFloat = Views.stack(splitChannels(image));
		RandomAccessibleInterval<FloatType> expected = colorGroup.apply(asFloat);
		RandomAccessibleInterval<FloatType> actual = colorGroup.apply(image);
		Utils.assertImagesEqual(actual, expected);
	}

	@Test
	public void testSplitChannels() {
		Img<ARGBType> image = ArrayImgs.argbs(new int[] { 0xaabbcc }, 1, 1);
		List<RandomAccessibleInterval<FloatType>> result = splitChannels(image);
		ImgLib2Assert.assertImageEquals(ArrayImgs.floats(new float[] { 0xaa }, 1, 1), result.get(0));
		ImgLib2Assert.assertImageEquals(ArrayImgs.floats(new float[] { 0xbb }, 1, 1), result.get(1));
		ImgLib2Assert.assertImageEquals(ArrayImgs.floats(new float[] { 0xcc }, 1, 1), result.get(2));
	}

	public static List<RandomAccessibleInterval<FloatType>> splitChannels(
		RandomAccessibleInterval<ARGBType> image)
	{
		Converter<ARGBType, FloatType> r = (in, out) -> out.setReal(ARGBType.red(in.get()));
		Converter<ARGBType, FloatType> g = (in, out) -> out.setReal(ARGBType.green(in.get()));
		Converter<ARGBType, FloatType> b = (in, out) -> out.setReal(ARGBType.blue(in.get()));
		return Arrays.asList(
			Converters.convert(image, r, new FloatType()),
			Converters.convert(image, g, new FloatType()),
			Converters.convert(image, b, new FloatType()));
	}

}
