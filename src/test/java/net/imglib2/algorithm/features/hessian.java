package net.imglib2.algorithm.features;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by arzt on 13.09.17.
 */
public class hessian {

	private static final double TWOPI = 2 * Math.PI;

	static double det(double a11, double a12, double a13, double a21, double a22, double a23, double a31, double a32, double a33) {
		return a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a11 * a23 * a32 - a12 * a21 * a33 - a13 * a22 * a31;
	}

	@Test
	public void testDet() {
		double a = det(1, 2, 3, 4, 5, 6, 7, 8, 9);
		assertEquals(0.0, a, 0.00001);
		double b = det(7, 3, 3, 4, 5, 9, 7, 8, 9);
		assertEquals(-117.0, b, 0.00001);
	}

	class Vector3D {
		public double x, y, z;

		@Override
		public String toString() {
			return x + ", " + y + ", " + z;
		}
	}

	public void eigenvalues(final Vector3D v, final double a11, final double a12, final double a13, final double a22, final double a23, final double a33) {
		final double a = -(a11 + a22 + a33);
		final double b = a11 * a22 + a11 * a33 + a22 * a33 - a12 * a12 - a13 * a13 - a23 * a23;
		final double c = a11 * (a23 * a23 - a22 * a33) + a22 * a13 * a13 + a33 * a12 * a12 - 2 * a12 * a13 * a23;
		final double q = (a * a - 3 * b) / 9;
		final double r = (a * a * a - 4.5 * a * b + 13.5 * c) / 27;
		final double sqrtq = (q > 0) ? Math.sqrt(q) : 0;
		final double sqrtq3 = sqrtq * sqrtq * sqrtq;
		if (sqrtq3 == 0) {
			v.x = 0;
			v.y = 0;
			v.z = 0;
		} else {
			final double rsqq3 = r / sqrtq3;
			final double angle = (rsqq3 * rsqq3 <= 1) ? Math.acos(rsqq3) : Math.acos(rsqq3 < 0 ? -1 : 1);
			v.x = -2 * sqrtq * Math.cos(angle / 3) - a / 3;
			v.y = -2 * sqrtq * Math.cos((angle + TWOPI) / 3) - a / 3;
			v.z = -2 * sqrtq * Math.cos((angle - TWOPI) / 3) - a / 3;
		}
	}

	public void abs(Vector3D v) {
		v.x = Math.abs(v.x);
		v.y = Math.abs(v.y);
		v.z = Math.abs(v.z);
	}

	public void sort(Vector3D v) {
		if(v.x < v.y) { double tmp = v.x; v.x = v.y; v.y = tmp; }
		if(v.y < v.z) {
			double tmp = v.y; v.y = v.z; v.z = tmp;
			if(v.x < v.z) { double tmp2 = v.x; v.x = v.z; v.z = tmp2; }
		}
	}

	public double checkEigenvalue(double lambda, double a11, double a12, double a13, double a22, double a23, double a33) {
		return det(a11 - lambda, a12, a13,
				a12, a22 - lambda, a23,
				a13, a23, a33 - lambda);
	}

	public void testEigenvalues(double a11, double a12, double a13, double a22, double a23, double a33) {
		Vector3D v = new Vector3D();
		eigenvalues(v, a11, a12, a13, a22, a23, a33);
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

