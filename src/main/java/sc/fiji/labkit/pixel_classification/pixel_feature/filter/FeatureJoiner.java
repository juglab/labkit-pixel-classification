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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter;

import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.gpu.GpuFeatureInput;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by arzt on 23.08.17.
 */
public class FeatureJoiner {

	private final GlobalSettings settings;

	private final List<FeatureOp> features;

	private final int count;

	public FeatureJoiner(List<FeatureOp> features) {
		this.settings = checkGlobalSettings(features);
		this.features = features;
		this.count = this.features.stream().mapToInt(FeatureOp::count).sum();
	}

	private GlobalSettings checkGlobalSettings(List<FeatureOp> features) {
		if (features.isEmpty())
			return null;
		GlobalSettings settings = features.get(0).globalSettings();
		boolean allEqual =
			features.stream().allMatch(f -> settings.equals(f.globalSettings()));
		if (!allEqual)
			throw new IllegalArgumentException(
				"All features in a feature group must use the same global settings");
		return settings;
	}

	public GlobalSettings globalSettings() {
		return settings;
	}

	public int count() {
		return count;
	}

	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		genericApply(output, (featureOp, o) -> featureOp.apply(input, o));
	}

	public void prefetch(GpuFeatureInput input) {
		for (FeatureOp feature : features)
			feature.prefetch(input);
	}

	public void apply(GpuFeatureInput input, List<GpuView> output) {
		genericApply(output, (featureOp, o) -> featureOp.apply(input, o));
	}

	public <T> void genericApply(List<T> output, BiConsumer<FeatureOp, List<T>> applyFeature) {
		if (output.size() != count)
			throw new IllegalArgumentException();
		int startIndex = 0;
		for (FeatureOp feature : features) {
			int count = feature.count();
			applyFeature.accept(feature, output.subList(startIndex, startIndex + count));
			startIndex += count;
		}
	}

	public List<String> attributeLabels() {
		List<String> labels = new ArrayList<>();
		features.stream().map(FeatureOp::attributeLabels).forEach(labels::addAll);
		return labels;
	}

	public List<FeatureOp> features() {
		return Collections.unmodifiableList(features);
	}
}
