package net.imglib2.algorithm.features.gson;

import com.google.gson.*;
import net.imagej.ops.OpEnvironment;
import net.imglib2.algorithm.features.*;
import net.imglib2.algorithm.features.ops.FeatureOp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class FeaturesGson {

	public static FeatureSettings fromJson(JsonElement json) {
		JsonObject json1 = json.getAsJsonObject();
		GlobalSettings globalSettings = new Gson().fromJson(json1.get("globals"), GlobalSettings.class);
		List<FeatureSetting> features = deserializeFeatureSettingsList(json1.get("ops").getAsJsonArray());
		return new FeatureSettings(globalSettings, features);
	}

	public static JsonElement toJsonTree(FeatureSettings featureSetting) {
		JsonObject object = new JsonObject();
		object.add("globals", new Gson().toJsonTree(featureSetting.globals()));
		object.add("ops", serialize(featureSetting.features()));
		return object;
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

}
