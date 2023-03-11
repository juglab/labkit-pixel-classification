/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.classification;

import com.google.gson.*;

import java.io.*;
import java.util.Base64;

/**
 * Helper for conversions between {@link weka.classifiers.Classifier} and
 * {@link JsonElement}.
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
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object bytesToObject(byte[] bytes) {
		InputStream stream = new ByteArrayInputStream(bytes);
		try {
			return new ObjectInputStream(stream).readObject();
		}
		catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonElement wekaToJson(weka.classifiers.Classifier classifier) {
		return new JsonPrimitive(Base64.getEncoder().encodeToString(objectToBytes(classifier)));
	}

	public static weka.classifiers.Classifier jsonToWeka(JsonElement jsonElement) {
		return (weka.classifiers.Classifier) bytesToObject(Base64.getDecoder().decode(jsonElement
			.getAsString()));
	}
}
