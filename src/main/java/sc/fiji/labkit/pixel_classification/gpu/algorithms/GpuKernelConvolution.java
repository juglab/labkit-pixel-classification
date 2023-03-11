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

package sc.fiji.labkit.pixel_classification.gpu.algorithms;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuScope;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;

import java.util.HashMap;

public class GpuKernelConvolution implements GpuNeighborhoodOperation {

	private final GpuApi gpu;

	private final Kernel1D kernel;

	private final int d;

	public GpuKernelConvolution(GpuApi gpu, Kernel1D kernel, int d) {
		this.gpu = gpu;
		this.kernel = kernel;
		this.d = d;
	}

	@Override
	public Interval getRequiredInputInterval(Interval targetInterval) {
		final long[] min = Intervals.minAsLongArray(targetInterval);
		final long[] max = Intervals.maxAsLongArray(targetInterval);
		min[d] += kernel.min();
		max[d] += kernel.max();
		return new FinalInterval(min, max);
	}

	@Override
	public void apply(GpuView input, GpuView output) {
		try (GpuApi scope = gpu.subScope()) {
			final Img<FloatType> kernelImage = ArrayImgs.floats(floats(kernel.fullKernel()), kernel
				.size());
			GpuImage kernelBuffer = scope.push(kernelImage);
			convolve(scope, kernelBuffer, input, output, d);
		}
	}

	private float[] floats(double[] doubles) {
		float[] floats = new float[doubles.length];
		for (int i = 0; i < doubles.length; i++)
			floats[i] = (float) doubles[i];
		return floats;
	}

	static void convolve(GpuApi gpu, GpuImage kernel, GpuView input, GpuView output, int d) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("kernelValues", kernel);
		GpuSeparableOperation.run(gpu, "convolve1d.cl", kernel.getWidth(), parameters, input, output,
			d);
	}
}
