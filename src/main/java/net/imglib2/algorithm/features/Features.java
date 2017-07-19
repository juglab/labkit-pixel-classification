package net.imglib2.algorithm.features;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.imagej.ops.special.function.Functions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import weka.core.Attribute;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class Features {

	public static RandomAccessibleInterval<FloatType> applyOnImg(Feature feature, RandomAccessibleInterval<FloatType> image) {
		Img<FloatType> result = RevampUtils.ops().create().img(RevampUtils.extend(image, 0, feature.count() - 1), new FloatType());
		feature.apply(Views.extendBorder(image), RevampUtils.slices(result));
		return result;
	}

	public static List<Attribute> attributes(Feature feature, List<String> classes) {
		List<String> labels = feature.attributeLabels();
		List<Attribute> attributes = new ArrayList<>();
		labels.forEach(label -> attributes.add(new Attribute(label)));
		attributes.add(new Attribute("class", classes));
		return attributes;
	}

	public static FeatureGroup fromJson(String serialized) {
		Gson gson = initGson();
		return gson.fromJson(serialized, FeatureGroup.class);
	}

	public static String toJson(Feature feature) {
		Gson gson = initGson();
		return gson.toJson(new FeatureGroup(feature), FeatureGroup.class);
	}

	private static Gson initGson() {
		return gsonModifiers(new GsonBuilder()).create();
	}

	public static GsonBuilder gsonModifiers(GsonBuilder builder) {
		return builder
				.registerTypeAdapter(Feature.class, new OpSerializer())
				.registerTypeAdapter(Feature.class, new OpDeserializer())
				.registerTypeAdapter(FeatureGroup.class, new FeatureGroupSerializer())
				.registerTypeAdapter(FeatureGroup.class, new FeatureGroupDeserializer());
	}

	public static <T extends FeatureOp> T create(Class<T> aClass, Object... args) {
		return (T) (Object) Functions.unary(RevampUtils.ops(), aClass, RandomAccessibleInterval.class, RandomAccessibleInterval.class, args);
	}

	static class FeatureGroupSerializer implements JsonSerializer<FeatureGroup> {

		@Override
		public JsonElement serialize(FeatureGroup f, Type type, JsonSerializationContext json) {
			Type collectionType = new TypeToken<List<Feature>>(){}.getType();
			return json.serialize(f.features(), collectionType);
		}
	}

	static class FeatureGroupDeserializer implements JsonDeserializer<FeatureGroup> {

		@Override
		public FeatureGroup deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext json) throws JsonParseException {
			Type collectionType = new TypeToken<List<Feature>>(){}.getType();
			return new FeatureGroup(json.<List<Feature>>deserialize(jsonElement, collectionType));
		}
	}

	static class OpSerializer implements JsonSerializer<Feature> {

		@Override
		public JsonElement serialize(Feature f, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = (JsonObject) jsonSerializationContext.serialize(f, f.getClass());
			jsonObject.add("class", new JsonPrimitive(f.getClass().getName()));
			return jsonObject;
		}
	}

	static class OpDeserializer implements JsonDeserializer<Feature> {

		@Override
		public Feature deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject o = jsonElement.getAsJsonObject();
			String className = o.get("class").getAsString();
			o.remove("class");
			try {
				return jsonDeserializationContext.deserialize(o, Class.forName(className));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
