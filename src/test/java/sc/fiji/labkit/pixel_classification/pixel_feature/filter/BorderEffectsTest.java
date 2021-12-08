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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.utils.CpuGpuRunner;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assume.assumeFalse;

/**
 * @author Matthias Arzt
 */
@RunWith(CpuGpuRunner.class)
public class BorderEffectsTest {

	public BorderEffectsTest(boolean useGpu) {
		this.useGpu = useGpu;
	}

	private final boolean useGpu;

	private Interval bigInterval = new FinalInterval(new long[] { 0, 0 }, new long[] { 150, 150 });

	private Interval interval = new FinalInterval(new long[] { 50, 50 }, new long[] { 100, 100 });

	private final Img<FloatType> fullImage = ImageJFunctions.convertFloat(Utils.loadImage(
		"bridge.png"));

	private RandomAccessibleInterval<FloatType> image = RevampUtils.copy(Views.interval(fullImage,
		bigInterval));

	@Test
	public void testGauss() {
		testFeature(GroupedFeatures.gauss());
	}

	@Test
	public void testHessian() {
		testFeature(GroupedFeatures.hessian());
	}

	@Deprecated
	@Test
	public void testGabor() {
		assumeFalse(useGpu);
		testFeature(GroupedFeatures.gabor());
	}

	@Test
	public void testDifferenceOfGaussians() {
		testFeature(GroupedFeatures.differenceOfGaussians());
	}

	@Test
	public void testLaplacian() {
		testFeature(GroupedFeatures.laplacian());
	}

	@Deprecated
	@Test
	public void testLipschitz() {
		assumeFalse(useGpu);
		testFeature(GroupedFeatures.lipschitz(50));
	}

	@Test
	public void testMin() {
		testFeature(GroupedFeatures.min());
	}

	@Test
	public void testMax() {
		testFeature(GroupedFeatures.max());
	}

	@Test
	public void testMean() {
		testFeature(GroupedFeatures.mean());
	}

	@Test
	public void testVariance() {
		testFeature(GroupedFeatures.variance());
	}

	public void testFeature(FeatureSetting feature) {
		FeatureCalculator calculator = FeatureCalculator.default2d().addFeature(feature).build();
		calculator.setUseGpu(useGpu);
		RandomAccessibleInterval<FloatType> expected = calculateExpected(calculator);
		RandomAccessibleInterval<FloatType> result = calculateResult(calculator);
		Utils.assertImagesEqual(120.0, result, expected);
	}

	public RandomAccessibleInterval<FloatType> calculateResult(FeatureCalculator feature) {
		return feature.apply(Views.extendBorder(image), interval);
	}

	public RandomAccessibleInterval<FloatType> calculateExpected(FeatureCalculator feature) {
		Interval featureInterval = RevampUtils.appendDimensionToInterval(interval, 0, feature.count() -
			1);
		return Views.interval(feature.apply(image), featureInterval);
	}
}
