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

package sc.fiji.labkit.pixel_classification.utils;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link CubicEquation}.
 */
public class CubicEquationTest {

	@Test
	public void testSolveNormalized() {
		// This test calculates the solution to the equation
		// 0 = x^3 - 14 x^2 + 41 x + 56
		// Which happens to be x = -1, 7 and 8
		double[] x = new double[3];
		CubicEquation.solveNormalized(56, 41, -14, x);
		assertArrayEquals(new double[] { -1, 7, 8 }, x, 0.0001);
	}

	@Test
	public void testSolveNormalizedZeros() {
		// This test calculates the solution to the equation
		// 0 = x^3 - 14 x^2 + 41 x + 56
		// Which happens to be x = -1, 7 and 8
		double[] x = new double[3];
		CubicEquation.solveNormalized(0, 0, 0, x);
		assertArrayEquals(new double[] { 0, 0, 0 }, x, 0.0001);
	}

	@Test
	public void testSolveNormalized233() {
		// This test calculates the solution to the equation
		// 0 = x^3 - 14 x^2 + 41 x + 56
		// Which happens to be x = -1, 7 and 8
		double[] x = new double[3];
		CubicEquation.solveNormalized(-18, 21, -8, x);
		assertArrayEquals(new double[] { 2, 3, 3 }, x, 0.0001);
	}

	@Test
	public void testSolveNormalized223() {
		// This test calculates the solution to the equation
		// 0 = x^3 - 14 x^2 + 41 x + 56
		// Which happens to be x = -1, 7 and 8
		double[] x = new double[3];
		CubicEquation.solveNormalized(-12, 16, -7, x);
		assertArrayEquals(new double[] { 2, 2, 3 }, x, 0.0001);
	}

	@Test
	public void testSolveScaled() {
		// This test calculates the solution to the equation
		// 0 = x^3 + -41 x + 56
		// Which happens to be x = -1, 7 and 8
		double[] x = new double[3];
		CubicEquation.solveScaled(56, -41, x);
		assertArrayEquals(new double[] { -7, 3.5 - Math.sqrt(4.25), 3.5 + Math.sqrt(4.25) }, x, 0.0001);
	}

	@Test
	public void testLog2() {
		assertEquals(0, CubicEquation.log2(1));
		assertEquals(1, CubicEquation.log2(2));
		assertEquals(1, CubicEquation.log2(3.9));
		assertEquals(2, CubicEquation.log2(4));
		assertEquals(-1, CubicEquation.log2(0.5));
	}

	@Test
	public void testLdexp() {
		assertEquals(2, CubicEquation.timesPowerOf2(1, 1), 0.0);
		assertEquals(4, CubicEquation.timesPowerOf2(1, 2), 0.0);
		assertEquals(0.25, CubicEquation.timesPowerOf2(1, -2), 0.0);
	}

	@Test
	public void testInvertible() {
		assertEquals(1, CubicEquation.timesPowerOf2(1, CubicEquation.log2((double) 1)), 0.0);
		assertEquals(2, CubicEquation.timesPowerOf2(1, CubicEquation.log2((double) 2)), 0.0);
		assertEquals(0.5, CubicEquation.timesPowerOf2(1, CubicEquation.log2(0.5)), 0.0);
		assertEquals(2, CubicEquation.timesPowerOf2(1, CubicEquation.log2((double) 3)), 0.0);
	}

	@Test
	public void testRoot() {
		assertEquals(4, root(16, 2), 0.0);
		assertEquals(2, root(16, 4), 0.0);
		assertEquals(0.5, CubicEquation.timesPowerOf2(1, CubicEquation.log2(0.5)), 0.0);
		assertEquals(2, CubicEquation.timesPowerOf2(1, CubicEquation.log2((double) 3)), 0.0);
	}

	@Test
	public void testPrecision() {
		Random random = new Random(42);
		double relativeError = 0;
		for (int i = 0; i < 1_000_000; i++) {
			double[] expectedX = new double[] { random.nextFloat(), random.nextFloat(), random
				.nextFloat() };
			Arrays.sort(expectedX);
			double[] a = initCoefficients(expectedX[0], expectedX[1], expectedX[2]);
			double[] x = new double[3];
			CubicEquation.solveNormalized(a[0], a[1], a[2], x);
			double error = maxError(expectedX, x);
			relativeError = Math.max(relativeError, error);
		}
		assertTrue(relativeError < 1e-8);
	}

	private double maxError(double[] expectedX, double[] x) {
		double max_error = 0;
		for (int i = 0; i < 3; i++) {
			max_error = Math.max(max_error, Math.abs(x[i] - expectedX[i]));
		}
		return max_error;
	}

	@Ignore("Precision for cubic equations with 3 very close roots is still a problem.")
	@Test
	public void testPrecision3() {
		double epsilon = 1e-6;
		double[] expectedX = new double[] { 0.65, 0.65 + epsilon, 0.65 + 2 * epsilon };
		Arrays.sort(expectedX);
		double[] a = initCoefficients(expectedX[0], expectedX[1], expectedX[2]);
		double[] x = new double[3];
		CubicEquation.solveNormalized(a[0], a[1], a[2], x);
		assertArrayEquals(expectedX, x, 1e-7);
	}

	private double log2(double precision) {
		return Math.log10(precision) / Math.log10(2);
	}

	private static double[] initCoefficients(double a, double b, double c) {
		return new double[] { -a * b * c, a * b + b * c + c * a, -a - b - c };
	}

	private static double root(double value, long exponent) {
		return CubicEquation.timesPowerOf2(1, CubicEquation.log2(value) / exponent);
	}
}
