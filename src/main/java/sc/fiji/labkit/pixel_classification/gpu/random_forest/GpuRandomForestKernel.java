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

package sc.fiji.labkit.pixel_classification.gpu.random_forest;

import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import net.imglib2.util.Intervals;

import java.util.HashMap;

public class GpuRandomForestKernel {

	private static final long ASSUMED_CONSTANT_MEMORY_SIZE = 64 * 1024;

	public static void randomForest(GpuApi gpu,
		GpuImage distributions,
		GpuImage src,
		GpuImage thresholds,
		GpuImage probabilities,
		GpuImage indices,
		int numberOfFeatures)
	{
		long[] globalSizes = src.getDimensions().clone();
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src", src);
		parameters.put("dst", distributions);
		parameters.put("thresholds", thresholds);
		parameters.put("probabilities", probabilities);
		parameters.put("indices", indices);

		HashMap<String, Object> constants = new HashMap<>();
		constants.put("NUMBER_OF_CLASSES", probabilities.getWidth());
		constants.put("NUMBER_OF_FEATURES", numberOfFeatures);
		constants.put("INDICES_SIZE", Intervals.numElements(indices.getDimensions()));
		constants.put("CONSTANT_OR_GLOBAL", appropriateMemory(thresholds, indices));
		gpu.execute(GpuRandomForestKernel.class, "random_forest.cl", "random_forest", globalSizes, null,
			parameters, constants);
	}

	private static String appropriateMemory(GpuImage thresholds, GpuImage indices) {
		long requiredConstantMemory = thresholds.clearCLBuffer().getSizeInBytes() + indices
			.clearCLBuffer().getSizeInBytes();
		boolean fitsConstantMemory = requiredConstantMemory < ASSUMED_CONSTANT_MEMORY_SIZE;
		return fitsConstantMemory ? "__constant" : "__global";
	}

	public static void findMax(GpuApi gpu,
		GpuImage distributions,
		GpuImage dst)
	{
		long[] globalSizes = { dst.getWidth(), dst.getHeight(), dst.getDepth() };
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("dst", dst);
		parameters.put("src", distributions);
		parameters.put("num_classes", (int) distributions.getNumberOfChannels());
		gpu.execute(GpuRandomForestKernel.class, "find_max.cl", "find_max", globalSizes, null,
			parameters, null);
	}
}
