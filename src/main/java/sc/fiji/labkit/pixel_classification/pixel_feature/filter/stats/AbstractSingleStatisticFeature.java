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

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.gpu.GpuFeatureInput;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Parameter;
import preview.net.imglib2.converter.RealTypeConverters;

import java.util.Collections;
import java.util.List;

abstract public class AbstractSingleStatisticFeature extends AbstractFeatureOp {
	@Parameter
	private double radius = 1;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList(filterName() + " filter radius=" + radius);
	}

	protected abstract String filterName();

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		int[] windowSize = getWindowSize();
		apply(windowSize, input.original(), output.get(0));
	}

	protected abstract void apply(int[] windowSize, RandomAccessible<FloatType> input, RandomAccessibleInterval<FloatType> output);

	@Override
	public void prefetch(GpuFeatureInput input) {
		input.prefetchOriginal(requiredSourceInterval(input.targetInterval()));
	}

	@Override
	public void apply(GpuFeatureInput input, List<GpuView> output) {
		try (GpuApi gpu = input.gpuApi().subScope()) {
			GpuView original = input.original(requiredSourceInterval(input.targetInterval()));
			int[] windowSize = getWindowSize();
			apply(gpu, windowSize, original, output.get(0));
		}
	}

	protected abstract void apply(GpuApi gpu, int[] windowSize, GpuView input, GpuView output);

	private int[] getWindowSize() {
		return globalSettings().pixelSize().stream()
				.mapToInt(pixelSize -> 1 + 2 * Math.max(0, (int) (radius / pixelSize)))
				.toArray();
	}

	private FinalInterval requiredSourceInterval(Interval targetInterval) {
		long[] borderSize = globalSettings().pixelSize().stream()
				.mapToLong(pixelSize -> Math.max(0, (long) (radius / pixelSize)))
				.toArray();
		return Intervals.expand(targetInterval, borderSize);
	}
}
