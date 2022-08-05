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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.dog2.DifferenceOfGaussiansFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.gauss.GaussianBlurFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient.GaussianGradientMagnitudeFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.hessian.HessianEigenvaluesFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.laplacian.LaplacianOfGaussianFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.MaxFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.MeanFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.VarianceFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.lipschitz.LipschitzFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.gabor.GaborFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.MinFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.structure.StructureTensorEigenvaluesFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @author Matthias Arzt
 */
public class GroupedFeatures {

	@Deprecated
	public static FeatureSetting gabor() {
		return createFeature(GaborFeature.class, "legacyNormalize", FALSE);
	}

	@Deprecated
	public static FeatureSetting legacyGabor() {
		return createFeature(GaborFeature.class, "legacyNormalize", TRUE);
	}

	public static FeatureSetting gauss() {
		return createFeature(GaussianBlurFeature.class);
	}

	public static FeatureSetting gradient() {
		return createFeature(GaussianGradientMagnitudeFeature.class);
	}

	public static FeatureSetting laplacian() {
		return createFeature(LaplacianOfGaussianFeature.class);
	}

	@Deprecated
	public static FeatureSetting lipschitz(long border) {
		return createFeature(LipschitzFeature.class, "border", border);
	}

	public static FeatureSetting hessian() {
		return createFeature(HessianEigenvaluesFeature.class);
	}

	public static FeatureSetting differenceOfGaussians() {
		return createFeature(DifferenceOfGaussiansFeature.class);
	}

	public static FeatureSetting structureTensor() {
		return createFeature(StructureTensorEigenvaluesFeature.class);
	}

	public static FeatureSetting min() {
		return createFeature(MinFeature.class);
	}

	public static FeatureSetting max() {
		return createFeature(MaxFeature.class);
	}

	public static FeatureSetting mean() {
		return createFeature(MeanFeature.class);
	}

	public static FeatureSetting variance() {
		return createFeature(VarianceFeature.class);
	}

	private static FeatureSetting createFeature(Class<? extends FeatureOp> aClass, Object... args) {
		return new FeatureSetting(aClass, args);
	}
}
