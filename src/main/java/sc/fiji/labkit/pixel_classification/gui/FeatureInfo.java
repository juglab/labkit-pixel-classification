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

package sc.fiji.labkit.pixel_classification.gui;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;

import java.lang.annotation.Annotation;

public class FeatureInfo {

	private final String label;
	private final Class<? extends FeatureOp> clazz;
	private final boolean hasParameters;

	public FeatureInfo(PluginInfo<FeatureOp> pluginInfo) throws InstantiableException {
		this.label = getLabel(pluginInfo);
		this.clazz = pluginInfo.loadClass();
		this.hasParameters = !new FeatureSetting(clazz).parameters().isEmpty();
	}

	public String getName() {
		return label;
	}

	public Class<? extends FeatureOp> pluginClass() {
		return clazz;
	}

	public boolean hasParameters() {
		return hasParameters;
	}

	public boolean isDeprecated() {
		return isDeprecated(clazz);
	}

	private static boolean isDeprecated(Class<? extends FeatureOp> aClass) {
		for (Annotation annotation : aClass.getAnnotations())
			if (Deprecated.class.equals(annotation.annotationType()))
				return true;
		return false;
	}

	private static String getLabel(PluginInfo<FeatureOp> pluginInfo) throws InstantiableException {
		String label = pluginInfo.getLabel();
		return label.isEmpty() ? pluginInfo.loadClass().getSimpleName() : label;
	}
}
