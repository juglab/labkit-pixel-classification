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

package sc.fiji.labkit.pixel_classification.utils;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ToString {

	public static void print(final RandomAccessibleInterval<?> image) {
		System.out.println(toString(image));
	}

	public static String toString(final RandomAccessibleInterval<?> image) {
		return toString(image, "");
	}

	private static <T> String toString(final RandomAccessibleInterval<T> image, final String prefix) {
		if (image.numDimensions() == 0) {
			return "{ numDimensions = 0 }";
		}
		if (image.numDimensions() == 1) {
			final StringJoiner joiner = new StringJoiner(", ", prefix + "{", "}");
			for (T pixel : Views.iterable(image))
				joiner.add(pixel.toString());
			return joiner.toString();
		}
		else {
			final StringJoiner joiner = new StringJoiner(",\n", prefix + "{\n", "\n" + prefix + "}");
			for (RandomAccessibleInterval<T> line : slices(image))
				joiner.add(toString(line, prefix + "\t"));
			return joiner.toString();
		}
	}

	private static <T> List<RandomAccessibleInterval<T>> slices(RandomAccessibleInterval<T> image) {
		int lastDim = image.numDimensions() - 1;
		long min = image.min(lastDim);
		long max = image.max(lastDim);
		List<RandomAccessibleInterval<T>> result = new ArrayList<>();
		for (long pos = min; pos <= max; pos++)
			result.add(Views.hyperSlice(image, lastDim, pos));
		return result;
	}
}
