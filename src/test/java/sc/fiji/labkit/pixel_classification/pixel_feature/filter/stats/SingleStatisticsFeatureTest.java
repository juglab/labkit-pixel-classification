/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.utils.CpuGpuRunner;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(CpuGpuRunner.class)
public class SingleStatisticsFeatureTest {

	public SingleStatisticsFeatureTest(boolean useGpu) {
		this.calculator.setUseGpu(useGpu);
		this.useGpu = useGpu;
	}

	private final boolean useGpu;

	FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleFeatures.min(1))
		.addFeature(SingleFeatures.max(1))
		.addFeature(SingleFeatures.mean(1))
		.addFeature(SingleFeatures.variance(1))
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
		ImgLib2Assert.assertImageEqualsRealType(Views.stack(expectedMin, expectedMax, expectedMean,
			expectedVariance),
			output, 1.e-6);
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
		FeatureCalculator calculator = FeatureCalculator.default2d()
				.addFeature(SingleFeatures.min(0))
				.addFeature(SingleFeatures.max(0))
				.addFeature(SingleFeatures.mean(0))
				.addFeature(SingleFeatures.variance(0))
				.build();
		calculator.setUseGpu(useGpu);
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
		calculator.apply(Views.extendBorder(input), output);
		ImgLib2Assert.assertImageEquals(Views.stack(expectedMin, expectedMax, expectedMean,
			expectedVariance),
			output);
	}

	@Test
	public void testAnisotropic() {
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.pixelSize(1, 2)
			.addFeature(SingleFeatures.max(1))
			.build();
		calculator.setUseGpu(useGpu);
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
		calculator.apply(Views.extendBorder(input), output);
		ImgLib2Assert.assertImageEquals(Views.stack(expectedMax), output);
	}
}
