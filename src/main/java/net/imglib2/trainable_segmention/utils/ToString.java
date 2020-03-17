
package net.imglib2.trainable_segmention.utils;

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
