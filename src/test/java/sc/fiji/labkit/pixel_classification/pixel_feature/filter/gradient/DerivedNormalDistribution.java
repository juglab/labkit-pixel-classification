/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient;

import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

import java.util.function.DoubleUnaryOperator;

@Deprecated
public final class DerivedNormalDistribution {

	private static final double ONE_DIVIDED_SQRT_TWO_PI = 1 / Math.sqrt(2 * Math.PI);

	private static final PolynomialFunction MINUS_X = new PolynomialFunction(new double[] { 0, -1 });

	private DerivedNormalDistribution() {
		// prevent from instantiation
	}

	public static double normalDistribution(double x) {
		return ONE_DIVIDED_SQRT_TWO_PI * Math.exp(-0.5 * x * x);
	}

	public static DoubleUnaryOperator derivedNormalDistribution(int derivative) {
		if (derivative == 0)
			return DerivedNormalDistribution::normalDistribution;
		else
			return x -> prefix(derivative).value(x) * normalDistribution(x);
	}

	public static PolynomialFunction prefix(int derivative) {
		if (derivative <= 0)
			return new PolynomialFunction(new double[] { 1 });
		else {
			PolynomialFunction p = prefix(derivative - 1);
			return p.polynomialDerivative().add(MINUS_X.multiply(p));
		}
	}

	public static DoubleUnaryOperator derivedGaussian(double sigma, int order) {
		DoubleUnaryOperator normal = derivedNormalDistribution(order);
		return x -> normal.applyAsDouble(x / sigma) / Math.pow(sigma, order + 1);
	}

	public static Kernel1D derivedGaussKernel(double sigma, int order) {
		if (sigma <= 0)
			throw new IllegalArgumentException("Sigma must be greater than zero.");
		final DoubleUnaryOperator operator = derivedGaussian(sigma, order);
		int width = (int) (sigma * kernelSizeFactor(order));
		return createKernel(operator, width);
	}

	private static double kernelSizeFactor(int order) {
		switch (order) {
			case 0:
				return 3;
			case 1:
				return 4;
			case 2:
				return 4;
			default:
				return 5;
		}
	}

	private static Kernel1D createKernel(DoubleUnaryOperator operator, int width) {
		double[] values = new double[width * 2 + 1];
		for (int i = 0, x = -width; i < values.length; i++, x++)
			values[i] = operator.applyAsDouble(x);
		return Kernel1D.centralAsymmetric(values);
	}
}
