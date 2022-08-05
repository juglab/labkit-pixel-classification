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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient;

import sc.fiji.labkit.pixel_classification.gpu.api.GpuPixelWiseOperation;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import sc.fiji.labkit.pixel_classification.gpu.GpuFeatureInput;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "gaussian gradient magnitude")
public class SingleGaussianGradientMagnitudeFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("gaussian gradient magnitude sigma=" + sigma);
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		final int n = globalSettings().numDimensions();
		if (n == 2)
			apply2d(input, output.get(0));
		else if (n == 3)
			apply3d(input, output.get(0));
		else
			throw new AssertionError();
	}

	private void apply3d(FeatureInput input, RandomAccessibleInterval<FloatType> output) {
		RandomAccessibleInterval<DoubleType> dx = derive(input, 0);
		RandomAccessibleInterval<DoubleType> dy = derive(input, 1);
		RandomAccessibleInterval<DoubleType> dz = derive(input, 2);
		LoopBuilder.setImages(dx, dy, dz, output).forEachPixel(
			(x, y, z, o) -> o.setReal(magnitude(x.getRealDouble(), y.getRealDouble(), z
				.getRealDouble())));
	}

	private void apply2d(FeatureInput input, RandomAccessibleInterval<FloatType> output) {
		RandomAccessibleInterval<DoubleType> dx = derive(input, 0);
		RandomAccessibleInterval<DoubleType> dy = derive(input, 1);
		LoopBuilder.setImages(dx, dy, output).forEachPixel(
			(x, y, o) -> o.setReal(magnitude(x.getRealDouble(), y.getRealDouble())));
	}

	private static double magnitude(double x, double y) {
		return Math.sqrt(square(x) + square(y));
	}

	private static double magnitude(double x, double y, double z) {
		return Math.sqrt(square(x) + square(y) + square(z));
	}

	private static double square(double x) {
		return x * x;
	}

	private RandomAccessibleInterval<DoubleType> derive(FeatureInput input, int d) {
		int[] orders = new int[globalSettings().numDimensions()];
		orders[d] = 1;
		return input.derivedGauss(sigma, orders);
	}

	@Override
	public void prefetch(GpuFeatureInput input) {
		for (int d = 0; d < globalSettings().numDimensions(); d++)
			input.prefetchDerivative(sigma, d, input.targetInterval());
	}

	@Override
	public void apply(GpuFeatureInput input, List<GpuView> output) {
		boolean is3d = globalSettings().numDimensions() == 3;
		GpuApi gpu = input.gpuApi();
		GpuPixelWiseOperation loopBuilder = GpuPixelWiseOperation.gpu(gpu);
		loopBuilder.addInput("dx", input.derivative(sigma, 0, input.targetInterval()));
		loopBuilder.addInput("dy", input.derivative(sigma, 1, input.targetInterval()));
		if (is3d)
			loopBuilder.addInput("dz", input.derivative(sigma, 2, input.targetInterval()));
		loopBuilder.addOutput("output", output.get(0));
		String operation = is3d ? "output = sqrt(dx * dx + dy * dy + dz * dz)"
			: "output = sqrt(dx * dx + dy * dy)";
		loopBuilder.forEachPixel(operation);
	}
}
