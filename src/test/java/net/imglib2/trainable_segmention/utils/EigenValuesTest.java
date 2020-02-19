
package net.imglib2.trainable_segmention.utils;

import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.EigenValues;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by arzt on 13.09.17.
 */
public class EigenValuesTest {

	static double det(double a11, double a12, double a13, double a21, double a22, double a23,
		double a31, double a32, double a33)
	{
		return a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a11 * a23 * a32 - a12 * a21 * a33 -
			a13 * a22 * a31;
	}

	@Test
	public void testDet() {
		double a = det(1, 2, 3, 4, 5, 6, 7, 8, 9);
		assertEquals(0.0, a, 0.00001);
		double b = det(7, 3, 3, 4, 5, 9, 7, 8, 9);
		assertEquals(-117.0, b, 0.00001);
	}

	public double checkEigenvalue(double lambda, double a11, double a12, double a13, double a22,
		double a23, double a33)
	{
		return det(a11 - lambda, a12, a13,
			a12, a22 - lambda, a23,
			a13, a23, a33 - lambda);
	}

	public void testEigenvalues(double a11, double a12, double a13, double a22, double a23,
		double a33)
	{
		EigenValues.Vector3D v = new EigenValues.Vector3D();
		EigenValues.eigenvalues(v, a11, a12, a13, a22, a23, a33);
		assertEquals(0.0, checkEigenvalue(v.x, a11, a12, a13, a22, a23, a33), 0.000001);
		assertEquals(0.0, checkEigenvalue(v.y, a11, a12, a13, a22, a23, a33), 0.000001);
		assertEquals(0.0, checkEigenvalue(v.z, a11, a12, a13, a22, a23, a33), 0.000001);
	}

	@Test
	public void testEigenvalues() {
		testEigenvalues(1, 2, 3, 4, 5, 6);
	}

	private double sqr(double v) {
		return v * v;
	}
}
