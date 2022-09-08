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

package sc.fiji.labkit.pixel_classification.demo.custom_pixel_feature;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.utils.CpuGpuRunner;

/**
 * Tests {@link PartialDerivativeInXFeature}.
 */
@RunWith(CpuGpuRunner.class)
public class PartialDerivativeInXFeatureTest {

	private final boolean useGpu;

	public PartialDerivativeInXFeatureTest(boolean useGpu) {
		this.useGpu = useGpu;
	}

	@Test
	public void test() {
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.addFeature(PartialDerivativeInXFeature.class)
			.build();
		calculator.setUseGpu(useGpu);
		RandomAccessibleInterval<FloatType> output = calculator.apply(Utils.dirac2d(), Intervals
			.createMinMax(-2, -2, 2, 2));
		float[] pixels = {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0.5f, 0, -0.5f, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
		};
		RandomAccessibleInterval<FloatType> expected = Views.translate(ArrayImgs.floats(pixels, 5, 5,
			1), -2, -2, 0);
		ImgLib2Assert.assertImageEquals(expected, output);
	}
}
