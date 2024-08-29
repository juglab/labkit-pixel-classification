/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
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

import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;

import java.util.ArrayList;
import java.util.List;

public class GpuNeighborhoodOperations {

	private GpuNeighborhoodOperations() {
		// prevent from instantiation
	}

	public static GpuNeighborhoodOperation concat(GpuApi gpu,
		List<? extends GpuNeighborhoodOperation> convolutions)
	{
		return new GpuConcatenatedNeighborhoodOperation(gpu, convolutions);
	}

	public static GpuNeighborhoodOperation min(GpuApi gpu, int[] windowSize) {
		return separableOperation(gpu, GpuSeparableOperation.Operation.MIN, windowSize);
	}

	public static GpuNeighborhoodOperation max(GpuApi gpu, int[] windowSize) {
		return separableOperation(gpu, GpuSeparableOperation.Operation.MAX, windowSize);
	}

	public static GpuNeighborhoodOperation mean(GpuApi gpu, int[] windowSize) {
		return separableOperation(gpu, GpuSeparableOperation.Operation.MEAN, windowSize);
	}

	public static GpuNeighborhoodOperation min1d(GpuApi gpu, int windowSize, int d) {
		return new GpuSeparableOperation(gpu, GpuSeparableOperation.Operation.MIN, windowSize, d);
	}

	public static GpuNeighborhoodOperation max1d(GpuApi gpu, int windowSize, int d) {
		return new GpuSeparableOperation(gpu, GpuSeparableOperation.Operation.MAX, windowSize, d);
	}

	public static GpuNeighborhoodOperation mean1d(GpuApi gpu, int windowSize, int d) {
		return new GpuSeparableOperation(gpu, GpuSeparableOperation.Operation.MEAN, windowSize, d);
	}

	// -- Helper methods --

	private static GpuNeighborhoodOperation separableOperation(GpuApi gpu,
		GpuSeparableOperation.Operation operation, int[] windowSize)
	{
		List<GpuNeighborhoodOperation> steps = new ArrayList<>();
		for (int d = 0; d < windowSize.length; d++)
			steps.add(new GpuSeparableOperation(gpu, operation, windowSize[d], d));
		return concat(gpu, steps);
	}

}
