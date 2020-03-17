
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.trainable_segmention.utils.CubicEquation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

/**
 * Created by arzt on 04.10.17.
 */
public class EigenValuesSymmetric3D<T extends RealType<T>, C extends RealType<C>> implements
	EigenValues<T, C>
{
	private final double[] x = new double[3];

	@Override
	public void compute(Composite<T> matrix, Composite<C> evs) {
		eigenvalues(matrix);
		copyTo(evs);
	}

	public void computeMagnitudeOfEigenvalues(Composite<T> matrix, Composite<C> evs) {
		eigenvalues(matrix);
		abs();
		sort();
		copyTo(evs);
	}

	private void eigenvalues(Composite<T> matrix) {
		final double a11 = matrix.get(0).getRealDouble();
		final double a12 = matrix.get(1).getRealDouble();
		final double a13 = matrix.get(2).getRealDouble();
		final double a22 = matrix.get(3).getRealDouble();
		final double a23 = matrix.get(4).getRealDouble();
		final double a33 = matrix.get(5).getRealDouble();
		final double b2 = -(a11 + a22 + a33);
		final double b1 = a11 * a22 + a11 * a33 + a22 * a33 - a12 * a12 - a13 * a13 - a23 * a23;
		final double b0 = a11 * (a23 * a23 - a22 * a33) + a22 * a13 * a13 + a33 * a12 * a12 - 2 * a12 * a13 * a23;
		CubicEquation.solveNormalized(b0, b1, b2, x);
	}

	private void abs() {
		x[0] = Math.abs(x[0]);
		x[1] = Math.abs(x[1]);
		x[2] = Math.abs(x[2]);
	}

	private void sort() {
		order(x, 0, 1);
		order(x, 1, 2);
		order(x, 0, 1);
	}

	private void order(double[] x, int smaller, int larger) {
		if (x[smaller] > x[larger]) {
			double tmp = x[smaller];
			x[smaller] = x[larger];
			x[larger] = tmp;
		}
	}

	private void copyTo(Composite<C> evs) {
		evs.get(0).setReal(x[2]);
		evs.get(1).setReal(x[1]);
		evs.get(2).setReal(x[0]);
	}

	@Override
	public EigenValues<T, C> copy() {
		return new EigenValuesSymmetric3D<>();
	}
}
