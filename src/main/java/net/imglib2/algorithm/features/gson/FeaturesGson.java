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

	public static FeatureGroup fromJson(OpEnvironment ops, JsonElement json) {
		JsonObject json1 = json.getAsJsonObject();
		GlobalSettings globalSettings = new Gson().fromJson(json1.get("globals"), GlobalSettings.class);
		List<FeatureSetting> features = deserializeFeatureSettingsList(json1.get("ops").getAsJsonArray());
		return Features.group(ops, globalSettings, features);
	}

	public static JsonElement toJsonTree(FeatureGroup featureGroup) {
		JsonObject object = new JsonObject();
		object.add("globals", new Gson().toJsonTree(featureGroup.globalSettings()));
		object.add("ops", serialize(featureGroup.features()));
		return object;
	}

	private static JsonElement serialize(List<FeatureOp> features) {
		JsonArray array = new JsonArray();
		features.forEach(feature -> array.add(FeatureSetting.fromOp(feature).toJsonTree()));
		return array;
	}

	private static List<FeatureSetting> deserializeFeatureSettingsList(JsonArray ops) {
		List<FeatureSetting> features = new ArrayList<>();
		ops.forEach(element -> features.add(FeatureSetting.fromJson(element)));
		return features;
	}

}
