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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.laplacian;

import sc.fiji.labkit.pixel_classification.gpu.api.GpuPixelWiseOperation;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.gpu.GpuFeatureInput;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imglib2.loops.LoopBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Plugin(type = FeatureOp.class, label = "laplacian of gaussian")
public class SingleLaplacianOfGaussianFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("laplacian of gaussian sigma=" + sigma);
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		int n = globalSettings().numDimensions();
		switch (n) {
			case 2:
				apply2d(input, output);
				return;
			case 3:
				apply3d(input, output);
				return;
			default:
				throw new IllegalArgumentException("Expect 2d or 3d.");
		}
	}

	private void apply2d(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		RandomAccessibleInterval<DoubleType> dx = input.derivedGauss(sigma, order(2, 0));
		RandomAccessibleInterval<DoubleType> dy = input.derivedGauss(sigma, order(2, 1));
		LoopBuilder.setImages(dx, dy, output.get(0)).multiThreaded().forEachPixel(
			(x, y, sum) -> sum.setReal(x.getRealDouble() + y.getRealDouble()));
	}

	private void apply3d(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		RandomAccessibleInterval<DoubleType> dx = input.derivedGauss(sigma, order(3, 0));
		RandomAccessibleInterval<DoubleType> dy = input.derivedGauss(sigma, order(3, 1));
		RandomAccessibleInterval<DoubleType> dz = input.derivedGauss(sigma, order(3, 2));
		LoopBuilder.setImages(dx, dy, dz, output.get(0)).multiThreaded().forEachPixel(
			(x, y, z, sum) -> sum.setReal(x.getRealDouble() + y.getRealDouble() + z.getRealDouble()));
	}

	private int[] order(int n, int d) {
		return IntStream.range(0, n).map(i -> i == d ? 2 : 0).toArray();
	}

	@Override
	public void prefetch(GpuFeatureInput input) {
		for (int d = 0; d < globalSettings().numDimensions(); d++)
			input.prefetchSecondDerivative(sigma, d, d, input.targetInterval());
	}

	@Override
	public void apply(GpuFeatureInput input, List<GpuView> output) {
		boolean is3d = globalSettings().numDimensions() == 3;
		GpuApi gpu = input.gpuApi();
		GpuPixelWiseOperation loopBuilder = GpuPixelWiseOperation.gpu(gpu);
		loopBuilder.addInput("dxx", input.secondDerivative(sigma, 0, 0, input.targetInterval()));
		loopBuilder.addInput("dyy", input.secondDerivative(sigma, 1, 1, input.targetInterval()));
		if (is3d)
			loopBuilder.addInput("dzz", input.secondDerivative(sigma, 2, 2, input.targetInterval()));
		loopBuilder.addOutput("output", output.get(0));
		String operation = is3d ? "output = dxx + dyy + dzz" : "output = dxx + dyy";
		loopBuilder.forEachPixel(operation);
	}
}
