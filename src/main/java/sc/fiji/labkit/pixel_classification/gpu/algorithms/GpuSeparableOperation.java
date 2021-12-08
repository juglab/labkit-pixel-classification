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

import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import net.imglib2.util.Intervals;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class GpuSeparableOperation implements GpuNeighborhoodOperation {

	enum Operation {
			MIN("min1d.cl"), MAX("max1d.cl"), MEAN("mean1d.cl");

		private final String name;

		Operation(String name) {
			this.name = name;
		}
	}

	private final GpuApi gpu;

	private final Operation operation;

	private final int windowSize;

	private final int d;

	GpuSeparableOperation(GpuApi gpu, Operation operation, int windowSize, int d) {
		this.gpu = gpu;
		this.operation = operation;
		this.windowSize = windowSize;
		this.d = d;
	}

	@Override
	public Interval getRequiredInputInterval(Interval targetInterval) {
		final long[] min = Intervals.minAsLongArray(targetInterval);
		final long[] max = Intervals.maxAsLongArray(targetInterval);
		min[d] -= windowSize / 2;
		max[d] += (windowSize - 1) / 2;
		return new FinalInterval(min, max);
	}

	@Override
	public void apply(GpuView input, GpuView output) {
		GpuSeparableOperation.run(gpu, operation.name, windowSize, new HashMap<>(), input, output, d);
	}

	static void run(GpuApi gpu, String kernelFile, long windowSize,
		HashMap<String, Object> parameters, GpuView input, GpuView output, int d)
	{
		parameters.put("input", input.source());
		parameters.put("output", output.source());
		HashMap<String, Object> defines = new HashMap<>();
		long[] localSizes = new long[3];
		Arrays.fill(localSizes, 1);
		localSizes[0] = output.dimensions().dimension(d);
		defines.put("KERNEL_LENGTH", windowSize);
		defines.put("BLOCK_SIZE", localSizes[0]);
		setSkips(defines, "INPUT", input, d);
		setSkips(defines, "OUTPUT", output, d);
		defines.put("OUTPUT_IMAGE_PARAMETER", "__global float* output");
		defines.put("INPUT_IMAGE_PARAMETER", "__global float* input");
		defines.put("OUTPUT_WRITE_PIXEL(x,y,z,v)",
			"output[OUTPUT_OFFSET + OUTPUT_X_SKIP * (x) + OUTPUT_Y_SKIP * (y) + OUTPUT_Z_SKIP * (z)] = v;");
		defines.put("INPUT_READ_PIXEL(x,y,z)",
			"input[INPUT_OFFSET + INPUT_X_SKIP * (x) + INPUT_Y_SKIP * (y) + INPUT_Z_SKIP * (z)]");
		long[] globalSizes = getDimensions(output.dimensions());
		ArrayUtils.swap(globalSizes, 0, d);
		gpu.execute(GpuKernelConvolution.class, kernelFile, "separable_operation",
			globalSizes, localSizes, parameters, defines);
	}

	public static void convolve(GpuApi gpu, GpuImage kernel, GpuImage input, int kernel_center,
		GpuImage output, int d)
	{
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("input", input);
		parameters.put("kernelValues", kernel);
		parameters.put("output", output);
		HashMap<String, Object> defines = new HashMap<>();
		long[] localSizes = new long[3];
		Arrays.fill(localSizes, 1);
		localSizes[0] = output.getDimensions()[d];
		defines.put("KERNEL_LENGTH", kernel.getWidth());
		defines.put("BLOCK_SIZE", localSizes[0]);
		defines.put("OUTPUT_IMAGE_PARAMETER", "IMAGE_output_TYPE output");
		defines.put("INPUT_IMAGE_PARAMETER", "IMAGE_input_TYPE input");
		defines.put("OUTPUT_WRITE_PIXEL(cx,cy,cz,v)",
			"WRITE_output_IMAGE(output, POS_output_INSTANCE(" + position(d, 0) + ",0), v)");
		defines.put("INPUT_READ_PIXEL(cx,cy,cz)",
			"READ_input_IMAGE(input, sampler, POS_input_INSTANCE(" + position(d, kernel_center) +
				",0)).x");
		long[] globalSizes = output.getDimensions();
		ArrayUtils.swap(globalSizes, 0, d);
		gpu.execute(GpuKernelConvolution.class, "convolve1d.cl", "separable_operation",
			globalSizes, localSizes, parameters, defines);
	}

	private static String position(int d, int kernel_center) {
		List<String> list = new ArrayList<>(Arrays.asList("(cx) - " + kernel_center, "(cy)", "(cz)"));
		Collections.swap(list, 0, d);
		return "(" + list.get(0) + "),(" + list.get(1) + "),(" + list.get(2) + ")";
	}

	private static void setSkips(HashMap<String, Object> defines, String prefix, GpuView view,
		int d)
	{
		GpuImage buffer = view.source();
		long[] skip = { 1, buffer.getWidth(), buffer.getWidth() * buffer.getHeight() };
		defines.put(prefix + "_OFFSET", view.offset());
		ArrayUtils.swap(skip, 0, d);
		defines.put(prefix + "_X_SKIP", skip[0]);
		defines.put(prefix + "_Y_SKIP", skip[1]);
		defines.put(prefix + "_Z_SKIP", skip[2]);
	}

	private static long[] getDimensions(Dimensions dimensions) {
		return new long[] {
			getDimension(dimensions, 0),
			getDimension(dimensions, 1),
			getDimension(dimensions, 2),
		};
	}

	private static long getDimension(Dimensions dimensions, int d) {
		return d < dimensions.numDimensions() ? dimensions.dimension(d) : 1;
	}
}
