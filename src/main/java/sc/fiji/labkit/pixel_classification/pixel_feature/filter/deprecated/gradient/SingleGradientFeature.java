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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.gradient;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Gradient")
public class SingleGradientFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Gradient_filter_" + sigma);
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		final int n = globalSettings().numDimensions();
		List<RandomAccessibleInterval<DoubleType>> derivatives = IntStream
			.range(0, n)
			.mapToObj(d -> derive(input, d)).collect(Collectors.toList());
		LoopBuilder.setImages(RevampUtils.vectorizeStack(derivatives), output.get(0)).forEachPixel((in,
			out) -> {
			double sum = 0;
			for (int i = 0; i < n; i++) {
				double x = in.get(i).getRealDouble();
				sum += x * x;
			}
			out.setReal(Math.sqrt(sum));
		});
	}

	private RandomAccessibleInterval<DoubleType> derive(FeatureInput input, int d) {
		int[] orders = IntStream.range(0, globalSettings().numDimensions())
			.map(i -> i == d ? 1 : 0).toArray();
		return input.derivedGauss(sigma * 0.4, orders);
	}
}
