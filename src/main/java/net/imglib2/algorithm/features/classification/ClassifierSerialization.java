package net.imglib2.algorithm.features.classification;

import com.google.gson.*;
import net.imagej.ops.OpEnvironment;
import net.imglib2.algorithm.features.gson.FeaturesGson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Base64;

/**
 * Helper to {@link Classifier} for loading and storing.
 *
 * @author Matthias Arzt
 */
class ClassifierSerialization {

	private final OpEnvironment ops;

	public ClassifierSerialization(OpEnvironment ops) {
		this.ops = ops;
	}

	public void store(Classifier classifier, OutputStream out) throws IOException {
		try( Writer fw = new OutputStreamWriter(out) ) {
			initGson().toJson(classifier, Classifier.class, fw);
		}
	}

	public Classifier load(InputStream in) throws IOException {
		try( Reader fr = new InputStreamReader(in) ) {
			return initGson().fromJson(fr, Classifier.class);
		}
	}

	private Gson initGson() {
		return FeaturesGson.gsonModifiers(ops, new GsonBuilder())
				.registerTypeHierarchyAdapter(OpEnvironment.class, new OpsSerialize())
				.registerTypeHierarchyAdapter(OpEnvironment.class, new OpsDeserialize())
				.registerTypeAdapter(weka.classifiers.Classifier.class, new WekaSerializer())
				.registerTypeAdapter(weka.classifiers.Classifier.class, new WekaDeserializer())
				.create();
	}

	private static byte[] objectToBytes(Object object) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object bytesToObject(byte[] bytes) {
		InputStream stream = new ByteArrayInputStream(bytes);
		try {
			return new ObjectInputStream(stream).readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static class OpsSerialize implements JsonSerializer<OpEnvironment> {

		@Override
		public JsonElement serialize(OpEnvironment opEnvironment, Type type, JsonSerializationContext json) {
			return new JsonObject();
		}
	}

	private class OpsDeserialize implements JsonDeserializer<OpEnvironment> {

		@Override
		public OpEnvironment deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext json) throws JsonParseException {
			return ops;
		}
	}

	private static class WekaSerializer implements JsonSerializer<weka.classifiers.Classifier> {

		@Override
		public JsonElement serialize(weka.classifiers.Classifier classifier, Type type, JsonSerializationContext json) {
			return new JsonPrimitive(Base64.getEncoder().encodeToString(objectToBytes(classifier)));
		}
	}

	private static class WekaDeserializer implements JsonDeserializer<weka.classifiers.Classifier> {

		@Override
		public weka.classifiers.Classifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext json) throws JsonParseException {
			return (weka.classifiers.Classifier) bytesToObject(Base64.getDecoder().decode(jsonElement.getAsString()));
		}
	}
}
