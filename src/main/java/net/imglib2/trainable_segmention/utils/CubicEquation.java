package net.imglib2.trainable_segmention.utils;

import static java.lang.Math.min;

public class CubicEquation {

	private static final double PRECISION = timesPowerOf2(1, -50);

	/**
	 * Calculates the solution of the equation:
	 * <p>
	 * {@code 0 == a[3] * x^3 + a[2] * x^2 + a[1] * x + a[0]}
	 * <p>
	 * This is a Java port of the C code that can be found here:
	 * <a href="http://www.uni-koeln.de/deiters/math/supplement.pdf">http://www.uni-koeln.de/deiters/math/supplement.pdf</a>
	 */
	public static void solve(double a0, double a1, double a2, double a3, double[] x) {
		if (a3 == 0)
			throw new IllegalArgumentException();
		double h = 1 / a3;
		solveNormalized(a0 * h, a1 * h, a2 * h, x);
	}

	public static void solveNormalized(double b0, double b1, double b2, double[] x) {
		double s = 1.0 / 3.0 * b2;
		double q = (2. * s * s - b1) * s + b0;
		double p = b1 - b2 * s;
		solveDepressed(q, p, x);
		x[0] = x[0] - s;
		x[1] = x[1] - s;
		x[2] = x[2] - s;
	}

	/**
	 * Calculates the solution of the equation:
	 * <p>
	 * {@code 0 == x^3 + b2 * x^2 + b1 * x + b0}
	 * <p>
	 * This is a Java port of the C code that can be found here:
	 * <a href="http://www.uni-koeln.de/deiters/math/supplement.pdf">http://www.uni-koeln.de/deiters/math/supplement.pdf</a>
	 */
	public static void solveDepressed(double b0, double b1, double[] x) {
		final long e0 = log2(b0) / 3;
		final long e1 = log2(b1) / 2;
		final long e = -1 - max(max(e0, e1), -20);
		final double scaleFactor = timesPowerOf2(1, -e);
		b1 = timesPowerOf2(b1, 2 * e);
		b0 = timesPowerOf2(b0, 3 * e);
		solveScaled(b0, b1, x);
		x[0] *= scaleFactor;
		x[1] *= scaleFactor;
		x[2] *= scaleFactor;
	}

	/**
	 * Calculates all 3 solutions of the equation: x^3 + b1 * x + b0 == 0
	 */
	static void solveScaled(double b0, double b1, double[] x) {
		double h = findRoot(b0, b1);
		x[2] = h;
		solve_quadratic(h * h + b1, h, x);
		if (x[1] > x[2]) swap(x, 1, 2);
		if (x[0] > x[1]) swap(x, 0, 1);
	}

	/**
	 * Calculates one solution of the equation: x^3 + b1 * x + b0 == 0
	 */
	private static double findRoot(double b0, double b1) {
		if (b0 == 0)
			return 0;

		final double w = max(abs(b0), abs(b1)) + 1.0;	/* radius of root circle */
		double x = b0 > 0.0 ? -w : w;
		double dx;
		do {
			dx = halleysMethod(b0, b1, x);
			x -= dx;
		} while (abs(dx) > abs(PRECISION * w));
		return x;
	}

	private static double halleysMethod(double b0, double b1, double x) {
		double dx;/* find 1st root by Halley's method */
		final double x_2 = x * x;
		final double y = (x_2 + b1) * x + b0;	/* ...looks odd, but saves CPU time */
		final double dy = 3 * x_2 + b1;
		dx = y * dy / (dy * dy - 3 * y * x);
		return dx;
	}

	private static void solve_quadratic(double c0, double c1, double[] x) {
		final double p = 0.5 * c1;			/* solve quadratic equation */
		double dis;
		dis = square(p) - c0;
		dis = dis > 0 ? sqrt(dis) : 0;
		x[0] = (-p - dis);
		x[1] = (-p + dis);
	}

	static long log2(double value) {
		long bits = Double.doubleToRawLongBits(value);
		return ((bits >> 52L) & 0x7ffL) - 1023L;
	}

	static double timesPowerOf2(double value, long exponent) {
		return value * Double.longBitsToDouble((exponent + 1023L) << 52L);
	}

	private static double max(double a, double b) {
		return (a > b) ? a : b;
	}

	private static long max(long a, long b) {
		return (a > b) ? a : b;
	}

	private static double abs(double value) {
		return Math.abs(value);
	}

	private static void swap(double[] x, int a, int b) {
		double t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	private static double square(double value) {
		return value * value;
	}

	private static double sqrt(double value) {
		return FastSqrt.sqrt(value);
	}
}
