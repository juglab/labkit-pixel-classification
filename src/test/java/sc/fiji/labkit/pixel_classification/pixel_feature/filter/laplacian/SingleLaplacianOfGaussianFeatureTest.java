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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.laplacian;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
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
public class SingleLaplacianOfGaussianFeatureTest {

	public SingleLaplacianOfGaussianFeatureTest(boolean useGpu) {
		this.calculator.setUseGpu(useGpu);
	}

	private final double sigma = 3.0;

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleLaplacianOfGaussianFeature.class, "sigma", sigma)
		.build();

	@Test
	public void testApply() {
		RandomAccessible<FloatType> input = Utils.dirac2d();
		RandomAccessibleInterval<FloatType> output = ArrayImgs.floats(10, 10);
		calculator.apply(input, Views.addDimension(output, 0, 0));
		RandomAccessibleInterval<FloatType> expected = Utils.create2dImage(output,
			(x, y) -> laplacianOfGaussian(sigma, x, y));
		Utils.assertImagesEqual(40, expected, output);
	}

	@Test
	public void testAttributes() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Arrays.asList("laplacian of gaussian sigma=3.0");
		assertEquals(expected, attributeLabels);
	}

	private double laplacianOfGaussian(double sigma, double x, double y) {
		double r_square = x * x + y * y;
		return (r_square / Math.pow(sigma, 4) - 2 * Math.pow(sigma, -2)) * Utils.gauss(sigma, x, y);
	}
}
