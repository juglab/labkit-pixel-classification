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

	public static JsonElement wekaToJson(weka.classifiers.Classifier classifier) {
		return new JsonPrimitive(Base64.getEncoder().encodeToString(objectToBytes(classifier)));
	}

	public static weka.classifiers.Classifier jsonToWeka(JsonElement jsonElement) {
		return (weka.classifiers.Classifier) bytesToObject(Base64.getDecoder().decode(jsonElement.getAsString()));
	}
}
