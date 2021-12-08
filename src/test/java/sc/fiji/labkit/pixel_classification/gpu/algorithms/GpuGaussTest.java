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

package sc.fiji.labkit.pixel_classification.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.gpu.api.AbstractGpuTest;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPool;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import static org.junit.Assume.assumeTrue;

/**
 * Tests {@link GpuGauss}.
 */
public class GpuGaussTest extends AbstractGpuTest {

	@Test
	public void test() {
		RandomAccessible<FloatType> dirac = Utils.dirac(3);
		RandomAccessibleInterval<FloatType> expected = RevampUtils.createImage(Intervals.createMinMax(
			-2, -2, -2, 2, 2, 2), new FloatType());
		Gauss3.gauss(2, dirac, expected);
		Interval targetInterval = new FinalInterval(expected);
		GpuNeighborhoodOperation operation = GpuGauss.gauss(gpu, 2, 2, 2);
		Interval inputInterval = operation.getRequiredInputInterval(targetInterval);
		try (
			GpuImage input = gpu.push(Views.interval(dirac, inputInterval));
			GpuImage output = gpu.create(Intervals.dimensionsAsLongArray(targetInterval),
				NativeTypeEnum.Float);)
		{
			operation.apply(GpuViews.wrap(input), GpuViews.wrap(output));
			RandomAccessibleInterval<FloatType> rai = gpu.pullRAI(output);
			ImgLib2Assert.assertImageEqualsRealType(Views.zeroMin(expected), rai, 1.e-7);
		}
	}
}
