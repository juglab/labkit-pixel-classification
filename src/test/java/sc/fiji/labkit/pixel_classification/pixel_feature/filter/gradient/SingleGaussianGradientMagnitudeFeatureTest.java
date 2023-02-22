/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.utils.CpuGpuRunner;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(CpuGpuRunner.class)
public class SingleGaussianGradientMagnitudeFeatureTest {

	public SingleGaussianGradientMagnitudeFeatureTest(boolean useGpu) {
		this.calculator.setUseGpu(useGpu);
	}

	private final double sigma = 3.0;

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleGaussianGradientMagnitudeFeature.class, "sigma", 3.0)
		.build();

	@Test
	public void testApply2d() {
		RandomAccessibleInterval<FloatType> result = ArrayImgs.floats(10, 10);
		calculator.apply(Utils.dirac2d(), Views.addDimension(result, 0, 0));
		RandomAccessibleInterval<FloatType> expected = Utils.create2dImage(result,
			(x, y) -> gradientMagnitudeOfGaussian(sigma, x, y));
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.004);
	}

	@Test
	public void testAttributeLabels() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Collections.singletonList("gaussian gradient magnitude sigma=3.0");
		assertEquals(expected, attributeLabels);
	}

	private double gradientMagnitudeOfGaussian(double sigma, double x, double y) {
		double r = Math.sqrt(x * x + y * y);
		return r / (sigma * sigma) * Utils.gauss(sigma, x, y);
	}
}
