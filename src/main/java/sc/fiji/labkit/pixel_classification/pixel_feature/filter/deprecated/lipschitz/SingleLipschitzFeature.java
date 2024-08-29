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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.lipschitz;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imglib2.loops.LoopBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Lipschitz")
public class SingleLipschitzFeature extends AbstractFeatureOp {

	@Parameter
	private double slope;

	@Parameter
	private long border;

	public SingleLipschitzFeature() {}

	public SingleLipschitzFeature(double slope, long border) {
		this.slope = slope;
		this.border = border;
	}

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		apply(input, output.get(0));
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	private void apply(FeatureInput in, RandomAccessibleInterval<FloatType> out) {
		RandomAccessible<FloatType> original = in.original();
		double[] pixelSize = globalSettings().pixelSizeAsDoubleArray();
		Interval expandedInterval = Intervals.expand(out, scaledBorder(pixelSize, out));
		RandomAccessibleInterval<FloatType> tmp = RevampUtils.createImage(expandedInterval,
			new FloatType());
		copy(original, tmp);
		ConeMorphology.performConeOperation(ConeMorphology.Operation.DILATION, tmp, scaledSlope(
			pixelSize));
		outEquals255PlusAMinusB(out, original, tmp); // out = 255 + in - tmp
	}

	private double[] scaledSlope(double[] pixelSizes) {
		return Arrays.stream(pixelSizes).map(pixelSize -> slope * pixelSize).toArray();
	}

	private long[] scaledBorder(double[] pixelSizes, RandomAccessibleInterval<FloatType> out) {
		return Arrays.stream(pixelSizes).mapToLong(pixelSize -> (long) Math.ceil(border / pixelSize))
			.toArray();
	}

	private <T extends Type<T>> void copy(RandomAccessible<T> in, RandomAccessibleInterval<T> out) {
		LoopBuilder.setImages(Views.interval(in, out), out).multiThreaded().forEachPixel((i, o) -> o
			.set(i));
	}

	private <T extends RealType<T>> void outEquals255PlusAMinusB(RandomAccessibleInterval<T> out,
		RandomAccessible<T> A, RandomAccessible<T> B)
	{
		T offset = Util.getTypeFromInterval(out).createVariable();
		offset.setReal(255);
		LoopBuilder.setImages(Views.interval(A, out), Views.interval(B, out), out).forEachPixel((a, b,
			o) -> {
			o.set(offset);
			o.sub(b);
			o.add(a);
		});
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Lipschitz_true_true_" + slope);
	}

}
