package net.imglib2.algorithm.features.classification;

import com.google.gson.*;
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

	private ClassifierSerialization() {
		// prevent form being instantiated
	}

	public static void store(Classifier classifier, String filename) throws IOException {
		try( FileWriter fw = new FileWriter(filename) ) {
			Gson gson = initGson();
			gson.toJson(classifier, Classifier.class, fw);
			fw.append("\n");
		}
	}

	public static Classifier load(String filename) throws IOException {
		try( FileReader fr = new FileReader(filename) ) {
			return initGson().fromJson(fr, Classifier.class);
		}
	}

	static Gson initGson() {
		return FeaturesGson.gsonModifiers(new GsonBuilder())
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

	static class WekaSerializer implements JsonSerializer<weka.classifiers.Classifier> {

		@Override
		public JsonElement serialize(weka.classifiers.Classifier classifier, Type type, JsonSerializationContext json) {
			return new JsonPrimitive(Base64.getEncoder().encodeToString(objectToBytes(classifier)));
		}
	}

	static class WekaDeserializer implements JsonDeserializer<weka.classifiers.Classifier> {

		@Override
		public weka.classifiers.Classifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext json) throws JsonParseException {
			return (weka.classifiers.Classifier) bytesToObject(Base64.getDecoder().decode(jsonElement.getAsString()));
		}
	}
}
