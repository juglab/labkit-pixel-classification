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
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.gpu.algorithms.GpuNeighborhoodOperations;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Plugin;
import net.imglib2.converter.RealTypeConverters;

@Plugin(type = FeatureOp.class, label = "max filter")
public class SingleMaxFeature extends AbstractSingleStatisticFeature {

	@Override
	protected String filterName() {
		return "max";
	}

	@Override
	protected void apply(int[] windowSize, RandomAccessible<FloatType> input, RandomAccessibleInterval<FloatType> output) {
		if(Intervals.numElements(windowSize) <= 1)
			RealTypeConverters.copyFromTo(input, output);
		else
			MinMaxFilter.maxFilter(windowSize).process(input, output);
	}

	@Override
	protected void apply(GpuApi gpu, int[] windowSize, GpuView input, GpuView output) {
		GpuNeighborhoodOperations.max(gpu, windowSize).apply(input, output);
	}

}
