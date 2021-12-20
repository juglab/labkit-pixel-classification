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
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.gpu.api.AbstractGpuTest;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;

import java.util.Arrays;

/**
 * Tests {@link GpuConcatenatedNeighborhoodOperation}.
 */
public class GpuConcatenatedNeighborhoodOperationTest extends AbstractGpuTest {

	@Test
	public void test() {
		Img<FloatType> dirac = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		GpuView input = GpuViews.wrap(gpu.push(dirac));
		GpuView output = GpuViews.wrap(gpu.create(new long[] { 3, 3 }, NativeTypeEnum.Float));
		GpuKernelConvolution a = new GpuKernelConvolution(gpu, Kernel1D.centralAsymmetric(1, 0, -1), 0);
		GpuKernelConvolution b = new GpuKernelConvolution(gpu, Kernel1D.centralAsymmetric(1, 2, 1), 1);
		GpuNeighborhoodOperation concatenation = GpuNeighborhoodOperations.concat(gpu, Arrays.asList(a,
			b));
		Interval inputInterval = concatenation.getRequiredInputInterval(Intervals.createMinMax(-1, -1,
			1, 1));
		Interval expectedInterval = Intervals.createMinMax(-2, -2, 2, 2);
		ImgLib2Assert.assertIntervalEquals(expectedInterval, inputInterval);
		concatenation.apply(input, output);
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(output.source());
		Img<FloatType> expected = ArrayImgs.floats(new float[] {
			-1, 0, 1,
			-2, 0, 2,
			-1, 0, 1
		}, 3, 3);
		ImgLib2Assert.assertImageEquals(expected, result);
	}
}
