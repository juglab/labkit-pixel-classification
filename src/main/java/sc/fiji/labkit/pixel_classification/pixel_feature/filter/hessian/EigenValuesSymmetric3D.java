
package net.imglib2.trainable_segmentation.pixel_feature.filter.hessian;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.trainable_segmentation.utils.CubicEquation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

import java.util.List;

/**
 * Created by arzt on 04.10.17.
 */
public class EigenValuesSymmetric3D<T extends RealType<T>, C extends RealType<C>> implements
	EigenValues<T, C>
{

	private final double[] x = new double[3];

	public static void calc(
		RandomAccessibleInterval<DoubleType> a11,
		RandomAccessibleInterval<DoubleType> a12,
		RandomAccessibleInterval<DoubleType> a13,
		RandomAccessibleInterval<DoubleType> a22,
		RandomAccessibleInterval<DoubleType> a23,
		RandomAccessibleInterval<DoubleType> a33,
		List<RandomAccessibleInterval<FloatType>> output)
	{
		Cursor<DoubleType> c11 = Views.flatIterable(a11).cursor();
		Cursor<DoubleType> c12 = Views.flatIterable(a12).cursor();
		Cursor<DoubleType> c13 = Views.flatIterable(a13).cursor();
		Cursor<DoubleType> c22 = Views.flatIterable(a22).cursor();
		Cursor<DoubleType> c23 = Views.flatIterable(a23).cursor();
		Cursor<DoubleType> c33 = Views.flatIterable(a33).cursor();
		Cursor<FloatType> o0 = Views.flatIterable(output.get(0)).cursor();
		Cursor<FloatType> o1 = Views.flatIterable(output.get(1)).cursor();
		Cursor<FloatType> o2 = Views.flatIterable(output.get(2)).cursor();
		double[] e = new double[3];
		while (o1.hasNext()) {
			final double v11 = c11.next().getRealDouble();
			final double v12 = c12.next().getRealDouble();
			final double v13 = c13.next().getRealDouble();
			final double v22 = c22.next().getRealDouble();
			final double v23 = c23.next().getRealDouble();
			final double v33 = c33.next().getRealDouble();
			calc(v11, v12, v13, v22, v23, v33, e);
			o0.next().setReal(e[2]);
			o1.next().setReal(e[1]);
			o2.next().setReal(e[0]);
		}
	}

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
		calc(a11, a12, a13, a22, a23, a33, x);
	}

	private static void calc(double a11, double a12, double a13, double a22,
		double a23, double a33, double[] x)
	{
		final double b2 = -(a11 + a22 + a33);
		final double b1 = a11 * a22 + a11 * a33 + a22 * a33 - a12 * a12 - a13 * a13 - a23 * a23;
		final double b0 = a11 * (a23 * a23 - a22 * a33) + a22 * a13 * a13 + a33 * a12 * a12 - 2 * a12 *
			a13 * a23;
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
