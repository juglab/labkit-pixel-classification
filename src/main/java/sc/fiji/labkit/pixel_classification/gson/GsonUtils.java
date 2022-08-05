/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Matthias Arzt
 */
final public class GsonUtils {

	private GsonUtils() {
		// prevent from instantiation.
	}

	private static Gson gson() {
		return new Gson();
	}

	public static void write(JsonElement json, String filename) {
		try (Writer writer = new FileWriter(filename)) {
			gson().toJson(json, writer);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonElement read(String filename) {
		try (Reader reader = new FileReader(filename)) {
			return gson().fromJson(reader, JsonElement.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void write(JsonElement json, OutputStream out) {
		try (Writer writer = new OutputStreamWriter(out)) {
			gson().toJson(json, writer);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonElement read(InputStream in) {
		try (Reader reader = new InputStreamReader(in)) {
			return gson().fromJson(reader, JsonElement.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(JsonElement json) {
		try (Writer writer = new StringWriter()) {
			gson().toJson(json, writer);
			return writer.toString();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonElement fromString(String text) {
		try (Reader reader = new StringReader(text)) {
			return gson().fromJson(reader, JsonElement.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
