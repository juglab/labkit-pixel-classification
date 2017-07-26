package net.imglib2.algorithm.features;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.imagej.ops.OpService;
import net.imagej.ops.special.function.Functions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import weka.core.Attribute;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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

	public static String toJson(FeatureGroup featureGroup) {
		Gson gson = initGson();
		return gson.toJson(featureGroup, FeatureGroup.class);
	}

	private static Gson initGson() {
		return gsonModifiers(new GsonBuilder()).create();
	}

	public static GsonBuilder gsonModifiers(GsonBuilder builder) {
		return builder
				.registerTypeAdapter(FeatureOp.class, new OpSerializer())
				.registerTypeAdapter(FeatureOp.class, new OpDeserializer(RevampUtils.ops(), GlobalSettings.defaultSettings()))
				.registerTypeAdapter(FeatureGroup.class, new FeatureGroupSerializer())
				.registerTypeAdapter(FeatureGroup.class, new FeatureGroupDeserializer());
	}

	public static <T extends FeatureOp> T create(Class<T> aClass, GlobalSettings globalSettings, Object... args) {
		Object[] allArgs = RevampUtils.prepend(globalSettings, args);
		return (T) (Object) Functions.unary(RevampUtils.ops(), aClass, RandomAccessibleInterval.class, RandomAccessibleInterval.class, allArgs);
	}

	public static FeatureGroup group(FeatureOp... features) {
		return new FeatureGroup(Arrays.asList(features));
	}

	public static FeatureGroup group(List<FeatureOp> features) {
		return new FeatureGroup(features);
	}

	static class FeatureGroupSerializer implements JsonSerializer<FeatureGroup> {

		@Override
		public JsonElement serialize(FeatureGroup f, Type type, JsonSerializationContext json) {
			Type collectionType = new TypeToken<List<FeatureOp>>(){}.getType();
			return json.serialize(f.features(), collectionType);
		}
	}

	static class FeatureGroupDeserializer implements JsonDeserializer<FeatureGroup> {

		@Override
		public FeatureGroup deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext json) throws JsonParseException {
			Type collectionType = new TypeToken<List<FeatureOp>>(){}.getType();
			return group(json.<List<FeatureOp>>deserialize(jsonElement, collectionType));
		}
	}

	static class OpSerializer implements JsonSerializer<FeatureOp> {

		@Override
		public JsonElement serialize(FeatureOp op, Type type, JsonSerializationContext jsonSerializationContext) {
			FeatureSetting fs = FeatureSetting.fromOp(op);
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("class", new JsonPrimitive(op.getClass().getName()));
			for(String parameter : fs.parameters())
				jsonObject.add(parameter, jsonSerializationContext.serialize(fs.getParameter(parameter)));
			return jsonObject;
		}
	}

	static class OpDeserializer implements JsonDeserializer<FeatureOp> {

		private final OpService opService;

		private GlobalSettings globalSettings;

		OpDeserializer(OpService opService, GlobalSettings globalSettings) {
			this.opService = opService;
			this.globalSettings = globalSettings;
		}

		@Override
		public FeatureOp deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject o = jsonElement.getAsJsonObject();
			String className = o.get("class").getAsString();
			o.remove("class");
			try {
				@SuppressWarnings("unchecked")
				Class<? extends FeatureOp> type1 = (Class<? extends FeatureOp>) Class.forName(className);
				FeatureSetting fs = FeatureSetting.fromClass(type1);
				for(String p : fs.parameters())
					fs.setParameter(p, jsonDeserializationContext.deserialize(o.get(p), fs.getParameterType(p)));
				return fs.newInstance(opService, globalSettings);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
