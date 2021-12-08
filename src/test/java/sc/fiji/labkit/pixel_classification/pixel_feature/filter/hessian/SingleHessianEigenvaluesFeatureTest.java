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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.hessian;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.utils.CpuGpuRunner;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(CpuGpuRunner.class)
public class SingleHessianEigenvaluesFeatureTest {

	public SingleHessianEigenvaluesFeatureTest(boolean useGpu) {
		this.calculator.setUseGpu(useGpu);
		this.calculator3d.setUseGpu(useGpu);
	}

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleHessianEigenvaluesFeature.class)
		.build();

	private final FeatureCalculator calculator3d = FeatureCalculator.default2d()
		.dimensions(3)
		.addFeature(SingleHessianEigenvaluesFeature.class)
		.build();

	@Test
	public void testApply2d() {
		Interval interval = Intervals.createMinMax(-10, -10, 10, 10);
		RandomAccessibleInterval<FloatType> input = Utils.create2dImage(interval, (x, y) -> x * x + 2 *
			y * y + 3 * x * y);
		RandomAccessibleInterval<FloatType> output = calculator.apply(input, new FinalInterval(1, 1));
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 6.162f,
			-0.16228f }, 1, 1, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, output, 0.001);
	}

	@Test
	public void testAttributeLabels2d() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Arrays.asList(
			"hessian - largest eigenvalue sigma=1.0",
			"hessian - smallest eigenvalue sigma=1.0");
		assertEquals(expected, attributeLabels);
	}

	@Test
	public void testApply3d() {
		Interval interval = Intervals.createMinMax(-10, -10, -10, 10, 10, 10);
		Converter<Localizable, FloatType> converter = (position, output) -> {
			double x = position.getDoublePosition(0);
			double y = position.getDoublePosition(1);
			double z = position.getDoublePosition(2);
			output.setReal(0.5 * x * x + 2 * x * y + 3 * x * z + 2 * y * y + 5 * y * z + 3 * z * z);
		};
		// The hessian of the input image is:
		// [1 2 3]
		// [2 4 5]
		// [3 5 6]
		float eigenvalue1 = 11.345f;
		float eigenvalue2 = 0.171f;
		float eigenvalue3 = -0.516f;
		RandomAccessibleInterval<FloatType> input = Converters.convert(new Localizables()
			.randomAccessibleInterval(interval), converter, new FloatType());
		RandomAccessibleInterval<FloatType> output = calculator3d.apply(input, new FinalInterval(1, 1,
			1));
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { eigenvalue1,
			eigenvalue2, eigenvalue3 }, 1, 1, 1, 3);
		ImgLib2Assert.assertImageEqualsRealType(output, expected, 0.001);
	}

	@Test
	public void testAttributeLabels3d() {
		List<String> attributeLabels = calculator3d.attributeLabels();
		List<String> expected = Arrays.asList(
			"hessian - largest eigenvalue sigma=1.0",
			"hessian - middle eigenvalue sigma=1.0",
			"hessian - smallest eigenvalue sigma=1.0");
		assertEquals(expected, attributeLabels);
	}
}
