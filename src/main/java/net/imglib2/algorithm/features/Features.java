package net.imglib2.algorithm.features;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.imagej.ops.Op;
import net.imagej.ops.OpInfo;
import net.imagej.ops.OpService;
import net.imagej.ops.special.function.Functions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.service.SciJavaService;
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
				.registerTypeAdapter(Feature.class, new OpDeserializer(RevampUtils.ops()))
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

	static class OpSerializer implements JsonSerializer<Op> {

		@Override
		public JsonElement serialize(Op op, Type type, JsonSerializationContext jsonSerializationContext) {
			CommandInfo commandInfo = new OpInfo(op.getClass()).cInfo();
			Module module = commandInfo.createModule(op);
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("class", new JsonPrimitive(op.getClass().getName()));
			for(ModuleItem<?> input : commandInfo.inputs()) {
				if(! SciJavaService.class.isAssignableFrom(input.getType()) &&
						! Arrays.asList("in", "out", "ops").contains(input.getName()) )
					jsonObject.add(input.getName(), jsonSerializationContext.serialize(input.getValue(module)));
			}
			return jsonObject;
		}
	}

	static class OpDeserializer implements JsonDeserializer<Op> {

		private final OpService opService;

		OpDeserializer(OpService opService) {
			this.opService = opService;
		}

		@Override
		public Op deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject o = jsonElement.getAsJsonObject();
			String className = o.get("class").getAsString();
			o.remove("class");
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Op> type1 = (Class<? extends Op>) Class.forName(className);
				OpInfo opInfo = new OpInfo(type1);
				Op op = jsonDeserializationContext.deserialize(o, type1);
				op.setEnvironment(opService);
				opService.getContext().inject(op);
				return op;
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
