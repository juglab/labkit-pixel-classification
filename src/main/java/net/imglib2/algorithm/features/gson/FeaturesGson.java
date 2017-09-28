package net.imglib2.algorithm.features.gson;

import com.google.gson.*;
import net.imagej.ops.OpEnvironment;
import net.imglib2.algorithm.features.*;
import net.imglib2.algorithm.features.ops.FeatureOp;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class FeaturesGson {

	public static FeatureGroup fromJson(String serialized, OpEnvironment ops) {
		Gson gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(FeatureGroup.class, new FeatureGroupDeserializer(ops))
				.create();
		return gson.fromJson(serialized, FeatureGroup.class);
	}

	public static String toJson(FeatureGroup featureGroup) {
		Gson gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(FeatureGroup.class, new FeatureGroupSerializer())
				.create();
		return gson.toJson(featureGroup, FeatureGroup.class);
	}

	public static GsonBuilder gsonModifiers(OpEnvironment ops, GsonBuilder builder) {
		return builder
				.registerTypeHierarchyAdapter(FeatureGroup.class, new FeatureGroupSerializer())
				.registerTypeHierarchyAdapter(FeatureGroup.class, new FeatureGroupDeserializer(ops));
	}

	private static class FeatureGroupSerializer implements JsonSerializer<FeatureGroup> {

		@Override
		public JsonElement serialize(FeatureGroup f, Type type, JsonSerializationContext json) {
			JsonObject object = new JsonObject();
			object.add("globals", json.serialize(f.globalSettings(), GlobalSettings.class));
			object.add("ops", serializeFeatures(f.features(), json));
			return object;
		}

		private JsonElement serializeFeatures(List<FeatureOp> features, JsonSerializationContext json) {
			JsonArray array = new JsonArray();
			features.forEach(feature -> array.add(serializeFeature(feature, json)));
			return array;
		}

		private JsonElement serializeFeature(FeatureOp op, JsonSerializationContext json) {
			FeatureSetting fs = FeatureSetting.fromOp(op);
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("class", new JsonPrimitive(op.getClass().getName()));
			for(String parameter : fs.parameters())
				jsonObject.add(parameter, json.serialize(fs.getParameter(parameter), fs.getParameterType(parameter)));
			return jsonObject;
		}
	}

	private static class FeatureGroupDeserializer implements JsonDeserializer<FeatureGroup> {

		private final OpEnvironment ops;

		FeatureGroupDeserializer(OpEnvironment ops) {
			this.ops = ops;
		}

		@Override
		public FeatureGroup deserialize(JsonElement element, Type type, JsonDeserializationContext json) throws JsonParseException {
			JsonObject object = element.getAsJsonObject();
			GlobalSettings globalSettings = json.deserialize(object.get("globals"), GlobalSettings.class);
			List<FeatureOp> features = deserializeFeatures(object.get("ops").getAsJsonArray(), globalSettings, json);
			return globalSettings.imageType().groupFactory().apply(features);
		}

		private List<FeatureOp> deserializeFeatures(JsonArray ops, GlobalSettings globalSettings, JsonDeserializationContext json) {
			List<FeatureOp> features = new ArrayList<>();
			ops.forEach(element -> features.add(deserializeFeature(element, globalSettings, json)));
			return features;
		}

		private FeatureOp deserializeFeature(JsonElement element, GlobalSettings globalSettings, JsonDeserializationContext json) {
			JsonObject o = element.getAsJsonObject();
			String className = o.get("class").getAsString();
			FeatureSetting fs = FeatureSetting.fromClass(classForName(className));
			for(String p : fs.parameters())
				fs.setParameter(p, json.deserialize(o.get(p), fs.getParameterType(p)));
			return fs.newInstance(ops, globalSettings);
		}

		private Class<? extends FeatureOp> classForName(String className) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends FeatureOp> tClass = (Class<? extends FeatureOp>) Class.forName(className);
				return tClass;
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
