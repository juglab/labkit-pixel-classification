
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

/**
 * Created by arzt on 04.10.17.
 */
public class EigenValuesSymmetric3D<T extends RealType<T>, C extends RealType<C>> implements
	EigenValues<T, C>
{

	private static final double TWOPI = 2 * Math.PI;

	private double x, y, z;

	@Override
	public void compute(Composite<T> matrix, Composite<C> evs) {
		eigenvalues(matrix);
		sort();
		copyTo(evs);
	}

	public void computeMagnitudeOfEigenvalues(Composite<T> matrix, Composite<C> evs) {
		eigenvalues(matrix);
		abs();
		sort();
		copyTo(evs);
	}

	@Override
	public EigenValues<T, C> copy() {
		return new EigenValuesSymmetric3D<>();
	}

	private void eigenvalues(Composite<T> matrix) {
		final double a11 = matrix.get(0).getRealDouble();
		final double a12 = matrix.get(1).getRealDouble();
		final double a13 = matrix.get(2).getRealDouble();
		final double a22 = matrix.get(3).getRealDouble();
		final double a23 = matrix.get(4).getRealDouble();
		final double a33 = matrix.get(5).getRealDouble();
		final double a = -(a11 + a22 + a33);
		final double b = a11 * a22 + a11 * a33 + a22 * a33 - a12 * a12 - a13 * a13 - a23 * a23;
		final double c = a11 * (a23 * a23 - a22 * a33) + a22 * a13 * a13 + a33 * a12 * a12 - 2 * a12 *
			a13 * a23;
		final double q = (a * a - 3 * b) / 9;
		final double r = (a * a * a - 4.5 * a * b + 13.5 * c) / 27;
		final double sqrtq = (q > 0) ? Math.sqrt(q) : 0;
		final double sqrtq3 = sqrtq * sqrtq * sqrtq;
		if (sqrtq3 == 0) {
			x = 0;
			y = 0;
			z = 0;
		}
		else {
			final double rsqq3 = r / sqrtq3;
			final double angle = (rsqq3 * rsqq3 <= 1) ? Math.acos(rsqq3) : Math.acos(rsqq3 < 0 ? -1 : 1);
			x = -2 * sqrtq * Math.cos(angle / 3) - a / 3;
			y = -2 * sqrtq * Math.cos((angle + TWOPI) / 3) - a / 3;
			z = -a - this.x - this.y;
		}
	}

	private void abs() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
	}

	private void sort() {
		if (x < y) {
			double tmp = x;
			x = y;
			y = tmp;
		}
		if (y < z) {
			double tmp = y;
			y = z;
			z = tmp;
			if (x < z) {
				double tmp2 = x;
				x = z;
				z = tmp2;
			}
		}
	}

	private void copyTo(Composite<C> evs) {
		evs.get(0).setReal(x);
		evs.get(1).setReal(y);
		evs.get(2).setReal(z);
	}

	@Override
	public String toString() {
		return x + ", " + y + ", " + z;
	}
}
