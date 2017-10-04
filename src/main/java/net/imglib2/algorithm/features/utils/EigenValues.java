package net.imglib2.algorithm.features.utils;

/**
 * Created by arzt on 04.10.17.
 */
public class EigenValues {

	private static final double TWOPI = 2 * Math.PI;

	public static void eigenvalues(final Vector3D v, final double a11, final double a12, final double a13, final double a22, final double a23, final double a33) {
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

	public static void abs(EigenValues.Vector3D v) {
		v.x = Math.abs(v.x);
		v.y = Math.abs(v.y);
		v.z = Math.abs(v.z);
	}

	public static void sort(EigenValues.Vector3D v) {
		if(v.x < v.y) { double tmp = v.x; v.x = v.y; v.y = tmp; }
		if(v.y < v.z) {
			double tmp = v.y; v.y = v.z; v.z = tmp;
			if(v.x < v.z) { double tmp2 = v.x; v.x = v.z; v.z = tmp2; }
		}
	}

	public static class Vector3D {
		public double x, y, z;

		@Override
		public String toString() {
			return x + ", " + y + ", " + z;
		}
	}
}
