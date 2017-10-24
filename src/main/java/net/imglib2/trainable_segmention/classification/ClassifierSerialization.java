package net.imglib2.trainable_segmention.classification;

import com.google.gson.*;

import java.io.*;
import java.util.Base64;

/**
 * Helper for conversions between {@link weka.classifiers.Classifier} and {@link JsonElement}.
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
