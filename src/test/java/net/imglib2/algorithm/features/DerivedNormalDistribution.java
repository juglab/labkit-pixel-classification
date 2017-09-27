package net.imglib2.algorithm.features;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class DerivedNormalDistribution {

	private static final double ONE_DEVIDED_SQRT_TWO_PI = 1 / Math.sqrt(2 * Math.PI);
	private PolynomialFunction MINUS_X = new PolynomialFunction(new double[]{0, -1});

	public double normalDistribution(double x) {
		return ONE_DEVIDED_SQRT_TWO_PI * Math.exp(- 0.5 * x * x);
	}

	public DoubleUnaryOperator derivedNormalDistribution(int derivative) {
		if(derivative == 0)
			return this::normalDistribution;
		else
			return x -> prefix(derivative).value(x) * normalDistribution(x);
	}

	public PolynomialFunction prefix(int derivative) {
		if(derivative <= 0)
			return new PolynomialFunction(new double[]{1});
		else {
			PolynomialFunction p = prefix(derivative - 1);
			return p.polynomialDerivative().add(MINUS_X.multiply(p));
		}
	}

	@Test
	public void testDerviceNormalDistribution() {
		DoubleUnaryOperator f = derivedNormalDistribution(5);
		assertEquals(0.9718373972, f.applyAsDouble(2.0), 0.00001);
		assertEquals(-1.4518243471, f.applyAsDouble(1.0), 0.00001);
		assertEquals(0.0, f.applyAsDouble(0.0), 0.00001);
	}
}