/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
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

public class ArrayUtils {

	private ArrayUtils() {
		// prevent from instantiation
	}

	public static int findMax(double[] values) {
		int maxIndex = 0;
		double max = values[maxIndex];
		for (int i = 1; i < values.length; i++) {
			if (max < values[i]) {
				maxIndex = i;
				max = values[maxIndex];
			}
		}
		return maxIndex;
	}

	public static int findMax(float[] value) {
		int maxIndex = 0;
		double max = value[maxIndex];
		for (int i = 1; i < value.length; i++) {
			if (max < value[i]) {
				maxIndex = i;
				max = value[maxIndex];
			}
		}
		return maxIndex;
	}

	public static double[] add(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++)
			b[i] += a[i];
		return b;
	}

	public static float[] add(float[] a, float[] b) {
		for (int i = 0; i < a.length; i++)
			b[i] += a[i];
		return b;
	}

	public static double[] normalize(double[] values) {
		double sum = sum(values);
		for (int i = 0; i < values.length; i++) {
			values[i] /= sum;
		}
		return values;
	}

	public static float[] normalize(float[] values) {
		float sum = sum(values);
		for (int i = 0; i < values.length; i++) {
			values[i] /= sum;
		}
		return values;
	}

	public static double sum(double[] values) {
		double sum = 0;
		for (double value : values)
			sum += value;
		return sum;
	}

	public static float sum(float[] values) {
		float sum = 0;
		for (float value : values)
			sum += value;
		return sum;
	}

	public static float[] toFloats(double[] values) {
		float[] result = new float[values.length];
		for (int i = 0; i < result.length; i++)
			result[i] = (float) values[i];
		return result;
	}
}
