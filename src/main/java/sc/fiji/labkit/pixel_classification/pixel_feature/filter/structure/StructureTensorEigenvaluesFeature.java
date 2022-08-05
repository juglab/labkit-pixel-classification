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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.structure;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractGroupFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import org.scijava.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(type = FeatureOp.class, label = "structure tensor eigenvalues (for each sigma)")
public class StructureTensorEigenvaluesFeature extends AbstractGroupFeatureOp {

	@Override
	protected List<FeatureSetting> initFeatures() {
		return globalSettings().sigmas().stream().flatMap(
			this::initFeaturesForSigma).collect(Collectors.toList());
	}

	private Stream<FeatureSetting> initFeaturesForSigma(Double sigma) {
		return Stream.of(1.0, 3.0).map(integrationScale -> new FeatureSetting(
			SingleStructureTensorEigenvaluesFeature.class, "sigma", sigma,
			"integrationScale", integrationScale));
	}
}
