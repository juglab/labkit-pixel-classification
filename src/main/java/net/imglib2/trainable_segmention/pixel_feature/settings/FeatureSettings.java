package net.imglib2.trainable_segmention.pixel_feature.settings;

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
		this.featureSettingList = featureSettingList.stream().map(FeatureSetting::new).collect(Collectors.toList());
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
		GlobalSettings globalSettings = new Gson().fromJson(object.get("globals"), GlobalSettings.class);
		List<FeatureSetting> features = deserializeFeatureSettingsList(object.get("ops").getAsJsonArray());
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
		if(!(obj instanceof FeatureSettings))
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
