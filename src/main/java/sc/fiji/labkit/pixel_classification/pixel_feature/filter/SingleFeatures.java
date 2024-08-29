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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient.SingleGaussianGradientMagnitudeFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.identity.IdentityFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.dog2.SingleDifferenceOfGaussiansFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.SingleMaxFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.SingleMeanFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.SingleMinFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.SingleVarianceFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.gabor.SingleGaborFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.gauss.SingleGaussianBlurFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.hessian.SingleHessianEigenvaluesFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.laplacian.SingleLaplacianOfGaussianFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.lipschitz.SingleLipschitzFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.structure.SingleStructureTensorEigenvaluesFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;

/**
 * @author Matthias Arzt
 */
public class SingleFeatures {

	public static FeatureSetting identity() {
		return createFeature(IdentityFeature.class);
	}

	@Deprecated
	public static FeatureSetting gabor(double sigma, double gamma, double psi, double frequency,
		int nAngles)
	{
		return gabor(sigma, gamma, psi, frequency, nAngles, false);
	}

	@Deprecated
	public static FeatureSetting legacyGabor(double sigma, double gamma, double psi, double frequency,
		int nAngles)
	{
		return gabor(sigma, gamma, psi, frequency, nAngles, true);
	}

	@Deprecated
	private static FeatureSetting gabor(double sigma, double gamma, double psi, double frequency,
		int nAngles, boolean legacyNormalize)
	{
		return createFeature(SingleGaborFeature.class, "sigma", sigma, "gamma", gamma, "psi", psi,
			"frequency", frequency, "nAngles", nAngles, "legacyNormalize", legacyNormalize);
	}

	public static FeatureSetting gauss(double sigma) {
		return createFeature(SingleGaussianBlurFeature.class, "sigma", sigma);
	}

	public static FeatureSetting gradient(double sigma) {
		return createFeature(SingleGaussianGradientMagnitudeFeature.class, "sigma", sigma);
	}

	@Deprecated
	public static FeatureSetting lipschitz(double slope, long border) {
		return createFeature(SingleLipschitzFeature.class, "slope", slope, "border", border);
	}

	public static FeatureSetting hessian(double sigma) {
		return createFeature(SingleHessianEigenvaluesFeature.class, "sigma", sigma);
	}

	public static FeatureSetting differenceOfGaussians(double sigma1, double sigma2) {
		return createFeature(SingleDifferenceOfGaussiansFeature.class, "sigma1", sigma1, "sigma2",
			sigma2);
	}

	public static FeatureSetting structureTensor(double sigma, double integrationScale) {
		return createFeature(SingleStructureTensorEigenvaluesFeature.class, "sigma", sigma,
			"integrationScale", integrationScale);
	}

	public static FeatureSetting laplacian(double sigma) {
		return createFeature(SingleLaplacianOfGaussianFeature.class, "sigma", sigma);
	}

	public static FeatureSetting min(double radius) {
		return createFeature(SingleMinFeature.class, "radius", radius);
	}

	public static FeatureSetting max(double radius) {
		return createFeature(SingleMaxFeature.class, "radius", radius);
	}

	public static FeatureSetting mean(double radius) {
		return createFeature(SingleMeanFeature.class, "radius", radius);
	}

	public static FeatureSetting variance(double radius) {
		return createFeature(SingleVarianceFeature.class, "radius", radius);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
