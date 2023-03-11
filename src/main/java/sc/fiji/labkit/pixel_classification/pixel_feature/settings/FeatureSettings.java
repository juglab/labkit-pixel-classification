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

package sc.fiji.labkit.pixel_classification.pixel_feature.settings;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class FeatureSettings {

	private final GlobalSettings globalSettings;

	private final List<FeatureSetting> featureSettingList;

	public FeatureSettings(GlobalSettings globalSettings, FeatureSetting... featureSetting) {
		this(globalSettings, Arrays.asList(featureSetting));
	}

	public FeatureSettings(GlobalSettings globalSettings, List<FeatureSetting> featureSettingList) {
		this.globalSettings = new GlobalSettings(globalSettings);
		this.featureSettingList = featureSettingList.stream().map(FeatureSetting::new).collect(
			Collectors.toList());
	}

	public GlobalSettings globals() {
		return globalSettings;
	}

	public List<FeatureSetting> features() {
		return featureSettingList;
	}

	public JsonElement toJson() {
		JsonObject object = new JsonObject();
		object.add("globals", new Gson().toJsonTree(globalSettings));
		object.add("ops", serialize(featureSettingList));
		return object;
	}

	public static FeatureSettings fromJson(JsonElement json) {
		JsonObject object = json.getAsJsonObject();
		GlobalSettings globalSettings = new Gson().fromJson(object.get("globals"),
			GlobalSettings.class);
		List<FeatureSetting> features = deserializeFeatureSettingsList(object.get("ops")
			.getAsJsonArray());
		return new FeatureSettings(globalSettings, features);
	}

	private static JsonElement serialize(List<FeatureSetting> features) {
		JsonArray array = new JsonArray();
		features.forEach(feature -> array.add(feature.toJsonTree()));
		return array;
	}

	private static List<FeatureSetting> deserializeFeatureSettingsList(JsonArray ops) {
		List<FeatureSetting> features = new ArrayList<>();
		ops.forEach(element -> features.add(FeatureSetting.fromJson(element)));
		return features;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FeatureSettings))
			return false;
		FeatureSettings fs = (FeatureSettings) obj;
		return this.globals().equals(fs.globals()) &&
			this.features().equals(fs.features());
	}

	@Override
	public int hashCode() {
		return Objects.hash(globals(), features());
	}
}
