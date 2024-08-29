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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.hessian;

import sc.fiji.labkit.pixel_classification.gpu.algorithms.GpuEigenvalues;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.gpu.GpuFeatureInput;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imglib2.loops.LoopBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(type = FeatureOp.class, label = "hessian eigenvalues")
public class SingleHessianEigenvaluesFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return globalSettings().numDimensions();
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		if (globalSettings().numDimensions() == 2)
			apply2d(input, output);
		else if (globalSettings().numDimensions() == 3)
			apply3d(input, output);
		else throw new AssertionError();
	}

	@Override
	public List<String> attributeLabels() {
		List<String> prefix = globalSettings().numDimensions() == 2 ? Arrays.asList("largest",
			"smallest") : Arrays.asList("largest", "middle", "smallest");
		return prefix.stream().map(s -> "hessian - " + s + " eigenvalue sigma=" + sigma)
			.collect(Collectors.toList());
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2 || globals.numDimensions() == 3;
	}

	private void apply2d(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		RandomAccessibleInterval<DoubleType> dxx = input.derivedGauss(sigma, 2, 0);
		RandomAccessibleInterval<DoubleType> dxy = input.derivedGauss(sigma, 1, 1);
		RandomAccessibleInterval<DoubleType> dyy = input.derivedGauss(sigma, 0, 2);
		RandomAccessibleInterval<FloatType> larger = output.get(0);
		RandomAccessibleInterval<FloatType> smaller = output.get(1);
		LoopBuilder.setImages(dxx, dxy, dyy, larger, smaller).multiThreaded().forEachPixel(
			(s_xx, s_yx, s_yy, l, s) -> calculateHessianPerPixel(s_xx.getRealDouble(), s_yx
				.getRealDouble(), s_yy.getRealDouble(), l, s));
	}

	private static void calculateHessianPerPixel(
		double s_xx, double s_xy, double s_yy, FloatType largerEigenvalue, FloatType smallerEigenvalue)
	{
		final double trace = s_xx + s_yy;
		float l = (float) (trace / 2.0 + Math.sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) /
			2.0);
		largerEigenvalue.set(l);
		float s = (float) (trace / 2.0 - Math.sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) /
			2.0);
		smallerEigenvalue.set(s);
	}

	private void apply3d(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		RandomAccessibleInterval<DoubleType> a11 = input.derivedGauss(sigma, 2, 0, 0);
		RandomAccessibleInterval<DoubleType> a12 = input.derivedGauss(sigma, 1, 1, 0);
		RandomAccessibleInterval<DoubleType> a13 = input.derivedGauss(sigma, 1, 0, 1);
		RandomAccessibleInterval<DoubleType> a22 = input.derivedGauss(sigma, 0, 2, 0);
		RandomAccessibleInterval<DoubleType> a23 = input.derivedGauss(sigma, 0, 1, 1);
		RandomAccessibleInterval<DoubleType> a33 = input.derivedGauss(sigma, 0, 0, 2);
		EigenValuesSymmetric3D.calc(a11, a12, a13, a22, a23, a33, output);
	}

	@Override
	public void prefetch(GpuFeatureInput input) {
		final Interval interval = input.targetInterval();
		final int n = interval.numDimensions();
		for (int d1 = 0; d1 < n; d1++)
			for (int d2 = d1; d2 < n; d2++)
				input.prefetchSecondDerivative(sigma, d1, d2, interval);
	}

	@Override
	public void apply(GpuFeatureInput input, List<GpuView> output) {
		final Interval interval = input.targetInterval();
		final int n = interval.numDimensions();
		final List<GpuView> derivatives = new ArrayList<>();
		for (int d1 = 0; d1 < n; d1++)
			for (int d2 = d1; d2 < n; d2++)
				derivatives.add(input.secondDerivative(sigma, d1, d2, interval));
		List<GpuView> eigenvalues = new ArrayList<>(output);
		GpuEigenvalues.symmetric(input.gpuApi(), derivatives, eigenvalues);
	}
}
