package clij;

import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;

import java.util.List;

public class CLIJEigenvalues {

	public static void symmetric(CLIJ2 clij, List<CLIJView> matrix, List<CLIJView> eigenvalues) {
		if (matrix.size() == 3 && eigenvalues.size() == 2)
			symmetric2d(clij, matrix.get(0), matrix.get(1), matrix.get(2), eigenvalues.get(0), eigenvalues.get(1));
		else if (matrix.size() == 6 && eigenvalues.size() == 3)
			symmetric3d(clij, matrix.get(0), matrix.get(1), matrix.get(2), matrix.get(3), matrix.get(4), matrix.get(5),
					eigenvalues.get(0), eigenvalues.get(1), eigenvalues.get(2));
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * Calculates the eigenvalues of a symmetric 2x2-matrices.
	 * All the images provided: xx, xy, yy, eigenvalue1, eigenvalue2 must have the
	 * same size. For every position in the image the pixels of xx, xy, yy define
	 * a symmetric 2x2-matrix [xx, xy; xy, yy] of which the eigenvalues are calculated.
	 * The larger eigenvalue is written to eigenvalue1, and the smaller eigenvalue ist written
	 * to eigenvalue2.
	 */
	public static void symmetric2d(CLIJ2 clij, CLIJView xx, CLIJView xy, CLIJView yy, CLIJView eigenvalue1, CLIJView eigenvalue2) {
		CLIJLoopBuilder.clij(clij)
				.addInput("s_xx", xx)
				.addInput("s_xy", xy)
				.addInput("s_yy", yy)
				.addOutput("l", eigenvalue1)
				.addOutput("s", eigenvalue2)
				.forEachPixel("float trace = s_xx + s_yy;" +
						"l = (float) (trace / 2.0 + sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) / 2.0);" +
						"s = (float) (trace / 2.0 - sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) / 2.0);");
	}

	/**
	 * Calculates the eigenvalues of the symmetric 3x3-matrices.
	 * All the images provided must have the same size.
	 * For every position in the image the pixels of xx, xy, zy, yy, yz and zz define
	 * a symmetric 3x3-matrix [xx, xy, xz; xy, yy, yz; xz, yz, zz] of which the eigenvalues are calculated.
	 * The largest eigenvalue is written to eigenvalue1, the middle eigenvalue is written to eigenvalue2,
	 * and the smallest eigenvalue ist written to eigenvalue3.
	 */
	public static void symmetric3d(CLIJ2 clij, CLIJView xx, CLIJView xy, CLIJView xz, CLIJView yy, CLIJView yz, CLIJView zz, CLIJView eigenvalue1, CLIJView eigenvalue2, CLIJView eigenvalue3) {

		CLIJLoopBuilder.clij(clij)
				.addInput("s_xx", xx)
				.addInput("s_xy", xy)
				.addInput("s_xz", xz)
				.addInput("s_yy", yy)
				.addInput("s_yz", yz)
				.addInput("s_zz", zz)
				.addOutput("large", eigenvalue1)
				.addOutput("middle", eigenvalue2)
				.addOutput("small", eigenvalue3)
				.forEachPixel( "double g_xx = s_xx, g_xy = s_xy, g_xz = s_xz, g_yy = s_yy, g_yz = s_yz, g_zz = s_zz;" +
						"double a = -(g_xx + g_yy + g_zz);" +
						"double b = g_xx * g_yy + g_xx * g_zz + g_yy * g_zz - g_xy * g_xy - g_xz * g_xz - g_yz * g_yz;" +
						"double c = g_xx * (g_yz * g_yz - g_yy * g_zz) + g_yy * g_xz * g_xz + g_zz * g_xy * g_xy - 2 * g_xy * g_xz * g_yz;" +
						"double x[3];" +
						"solve_cubic_equation(c, b, a, x);" +
						"large = (float) x[2];" +
						"middle = (float) x[1];" +
						"small = (float) x[0];");
	}
}
