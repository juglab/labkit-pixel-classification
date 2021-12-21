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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.dog;

import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.plugin.Plugin;
import net.imglib2.loops.LoopBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Difference of Gaussians (for each sigma)")
public class DifferenceOfGaussiansFeature extends AbstractFeatureOp {

	private List<Double> sigmas;
	private List<Pair<Double, Double>> sigmaPairs;

	private List<Pair<Double, Double>> sigmaPairs() {
		List<Pair<Double, Double>> sigmaPairs = new ArrayList<>();
		for (double sigma1 : sigmas)
			for (double sigma2 : sigmas)
				if (sigma2 < sigma1)
					sigmaPairs.add(new ValuePair<>(sigma1, sigma2));
		return sigmaPairs;
	}

	@Override
	public void initialize() {
		sigmas = globalSettings().sigmas();
		sigmaPairs = sigmaPairs();
	}

	@Override
	public int count() {
		return sigmaPairs.size();
	}

	@Override
	public List<String> attributeLabels() {
		return sigmaPairs.stream().map(pair -> "Difference_of_gaussians_" + pair.getA() + "_" + pair
			.getB())
			.collect(Collectors.toList());
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		for (int i = 0; i < output.size(); i++) {
			Pair<Double, Double> sigma1and2 = sigmaPairs.get(i);
			RandomAccessibleInterval<FloatType> target = output.get(i);
			subtract(input.gauss(sigma1and2.getB() * 0.4), input.gauss(sigma1and2.getA() * 0.4), target);
		}
	}

	private static void subtract(RandomAccessibleInterval<? extends RealType<?>> minuend,
		RandomAccessibleInterval<? extends RealType<?>> subtrahend,
		RandomAccessibleInterval<FloatType> target)
	{
		LoopBuilder.setImages(minuend, subtrahend, target).multiThreaded().forEachPixel(
			(m, s, t) -> t.setReal(m.getRealFloat() - s.getRealFloat()));
	}
}
