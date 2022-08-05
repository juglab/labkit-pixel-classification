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

package sc.fiji.labkit.pixel_classification.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.util.Intervals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class GpuConcatenatedNeighborhoodOperation implements GpuNeighborhoodOperation {

	private final GpuApi gpu;

	private final List<? extends GpuNeighborhoodOperation> convolutions;

	public GpuConcatenatedNeighborhoodOperation(GpuApi gpu,
		List<? extends GpuNeighborhoodOperation> convolutions)
	{
		this.gpu = gpu;
		this.convolutions = convolutions;
	}

	@Override
	public Interval getRequiredInputInterval(Interval targetInterval) {
		return intervals(targetInterval).get(0);
	}

	@Override
	public void apply(GpuView input, GpuView output) {
		try (GpuApi scope = gpu.subScope()) {
			List<Interval> intervals = intervals(new FinalInterval(output.dimensions()));
			if (!Intervals.equalDimensions(new FinalInterval(input.dimensions()), intervals.get(0)))
				throw new IllegalArgumentException("Dimensions of the input image are not as expected.");
			int n = convolutions.size();
			List<GpuView> buffers = new ArrayList<>(n);
			buffers.add(input);
			for (int i = 1; i < n; i++) {
				long[] dimensions = Intervals.dimensionsAsLongArray(intervals.get(i));
				GpuImage buffer = scope.create(dimensions, NativeTypeEnum.Float);
				buffers.add(GpuViews.wrap(buffer));
			}
			buffers.add(output);
			for (int i = 0; i < convolutions.size(); i++) {
				GpuNeighborhoodOperation convolution = convolutions.get(i);
				convolution.apply(buffers.get(i), buffers.get(i + 1));
			}
		}
	}

	private List<Interval> intervals(Interval outputInterval) {
		int n = convolutions.size();
		List<Interval> intervals = new ArrayList<>(n + 1);
		intervals.add(outputInterval);
		Interval t = outputInterval;
		for (int i = n - 1; i >= 0; i--) {
			GpuNeighborhoodOperation convolution = convolutions.get(i);
			t = convolution.getRequiredInputInterval(t);
			intervals.add(t);
		}
		Collections.reverse(intervals);
		return intervals;
	}
}
