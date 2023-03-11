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

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * ImgLib2 version of trainable segmentation's Sobel feature.
 * 
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Sobel Gradient")
public class SingleSobelGradientFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(FeatureInput in, List<RandomAccessibleInterval<FloatType>> out) {
		calculate(in.original(), out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Sobel_filter_" + sigma);
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	private void calculate(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		double[] sigmas = { sigma * 0.4, sigma * 0.4 };

		Interval dxInputInterval = RevampUtils.deriveXRequiredInput(out);
		Interval dyInputInterval = RevampUtils.deriveYRequiredInput(out);
		Interval blurredInterval = Intervals.union(dxInputInterval, dyInputInterval);

		RandomAccessibleInterval<FloatType> blurred = RevampUtils.gauss(in, blurredInterval, sigmas);
		RandomAccessibleInterval<FloatType> dx = RevampUtils.deriveX(blurred, out);
		RandomAccessibleInterval<FloatType> dy = RevampUtils.deriveY(blurred, out);
		RandomAccessible<Pair<FloatType, FloatType>> derivatives = Views.pair(dx, dy);
		mapToFloat(derivatives, out, input -> norm2(input.getA().get(), input.getB().get()));
	}

	private <I> void mapToFloat(RandomAccessible<I> in, RandomAccessibleInterval<FloatType> out,
		ToDoubleFunction<I> operation)
	{
		Views.interval(Views.pair(in, out), out)
			.forEach(p -> p.getB().set((float) operation.applyAsDouble(p.getA())));
	}

	private static double norm2(float x, float y) {
		return Math.sqrt(x * x + y * y);
	}
}
