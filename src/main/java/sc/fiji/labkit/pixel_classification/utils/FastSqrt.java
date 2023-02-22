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

public class FastSqrt {

	private FastSqrt() {
		// prevent from instantiation
	}

	public static float sqrt(float number) {
		return number * inverseSqrt(number);
	}

	private static float inverseSqrt(float number) {
		float x2 = number * 0.5F;
		float y = fastInverseSqrt(number);
		y = y * (1.5f - (x2 * y * y)); // Newton
		y = y * (1.5f - (x2 * y * y)); // Newton
		y = y * (1.5f - (x2 * y * y)); // Newton
		y = y * (1.5f - (x2 * y * y)); // Newton
		return y;
	}

	private static float fastInverseSqrt(float number) {
		return Float.intBitsToFloat(0x5f3759df - (Float.floatToRawIntBits(number) >> 1));
	}

	public static double sqrt(double number) {
		return number * inverseSqrt(number);
	}

	private static double inverseSqrt(double number) {
		double x2 = number * 0.5;
		double y = fastInverseSqrt(number);
		y = y * (1.5 - (x2 * y * y)); // Newton
		y = y * (1.5 - (x2 * y * y)); // Newton
		y = y * (1.5 - (x2 * y * y)); // Newton
		y = y * (1.5 - (x2 * y * y)); // Newton
		y = y * (1.5 - (x2 * y * y)); // Newton
		return y;
	}

	private static double fastInverseSqrt(double number) {
		return Double.longBitsToDouble(0x5FE6EB50C7B537A9L - (Double.doubleToRawLongBits(
			number) >> 1L));
	}
}
