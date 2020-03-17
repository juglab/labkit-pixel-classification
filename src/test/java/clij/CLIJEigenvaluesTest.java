package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.utils.AutoClose;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CLIJEigenvaluesTest {

	CLIJ2 clij = CLIJ2.getInstance();
	AutoClose autoClose = new AutoClose();

	@Test
	public void testCalculate2d() {
		try(
			CLIJView xx = image(1);
			CLIJView xy = image(2);
			CLIJView yy = image(3);
			CLIJView eigenvalue1 = image(0);
			CLIJView eigenvalue2 = image(0)
		) {
			CLIJEigenvalues.symmetric2d(clij, xx, xy, yy, eigenvalue1, eigenvalue2);
			assertEquals(2 + Math.sqrt(5), getValue(eigenvalue1), 0.0001);
			assertEquals(2 - Math.sqrt(5), getValue(eigenvalue2), 0.0001);
		}
	}

	@Test
	public void testCalculate3d() {
		try(
				CLIJView xx = image(1);
				CLIJView xy = image(2);
				CLIJView xz = image(3);
				CLIJView yy = image(4);
				CLIJView yz = image(5);
				CLIJView zz = image(6);
				CLIJView eigenvalue1 = image(0);
				CLIJView eigenvalue2 = image(0);
				CLIJView eigenvalue3 = image(0)
		) {
			CLIJEigenvalues.symmetric3d(clij, xx, xy, xz, yy, yz, zz, eigenvalue1, eigenvalue2, eigenvalue3);
			assertEquals(11.345, getValue(eigenvalue1), 0.001);
			assertEquals(0.171, getValue(eigenvalue2), 0.001);
			assertEquals(-0.516, getValue(eigenvalue3), 0.001);
		}
	}

	@Test
	public void testCubicEquation() {
		assertArrayEquals(new float[]{-1, 7, 8}, solveCubicEquation(56, 41, -14), 0.0001f);
		assertArrayEquals(new float[]{0, 0, 0}, solveCubicEquation(0, 0, 0), 0.0001f);
	}

	private float[] solveCubicEquation(float b0, float b1, float b2) {
		long[] dims = {1, 1};
		try (
				ClearCLBuffer x0 = clij.create(dims, NativeTypeEnum.Float);
				ClearCLBuffer x1 = clij.create(dims, NativeTypeEnum.Float);
				ClearCLBuffer x2 = clij.create(dims, NativeTypeEnum.Float);
		) {
			CLIJLoopBuilder.clij(clij)
					.addInput("b0", b0)
					.addInput("b1", b1)
					.addInput("b2", b2)
					.addOutput("x0", x0)
					.addOutput("x1", x1)
					.addOutput("x2", x2)
					.forEachPixel("double x[3];" +
							"solve_cubic_equation(b0, b1, b2, x);" +
							"x0 = x[0]; x1 = x[1]; x2 = x[2];");
			return new float[]{ getValue(x0), getValue(x1), getValue(x2) };
		}
	}

	@Test
	public void testPrecision2() {
		Random random = new Random(42);
		int n = 10000;
		Img<FloatType> expected = ArrayImgs.floats(3, n);
		Cursor<FloatType> cursor = expected.cursor();
		for (int i = 0; i < n; i++) {
			double[] expectedX = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
			Arrays.sort(expectedX);
			cursor.next().setReal(expectedX[0]);
			cursor.next().setReal(expectedX[1]);
			cursor.next().setReal(expectedX[2]);
		}
		Img<FloatType> zeros = ArrayImgs.floats(1, n);
		try (
			ClearCLBuffer expectedBuffer = clij.push(expected);
			ClearCLBuffer zeroBuffer = clij.push(zeros);
			ClearCLBuffer resultBuffer = clij.create(new long[]{3, n});
		) {
			CLIJView xx = CLIJView.interval(expectedBuffer, Intervals.createMinSize(0, 0, 1, n));
			CLIJView yy = CLIJView.interval(expectedBuffer, Intervals.createMinSize(1, 0, 1, n));
			CLIJView zz = CLIJView.interval(expectedBuffer, Intervals.createMinSize(2, 0, 1, n));
			CLIJView zero = CLIJView.wrap(zeroBuffer);
			CLIJView e1 = CLIJView.interval(resultBuffer, Intervals.createMinSize(0, 0, 1, n));
			CLIJView e2 = CLIJView.interval(resultBuffer, Intervals.createMinSize(1, 0, 1, n));
			CLIJView e3 = CLIJView.interval(resultBuffer, Intervals.createMinSize(2, 0, 1, n));
			CLIJEigenvalues.symmetric3d(clij, yy, zero, zero, xx, zero, zz, e3, e2, e1);
			RandomAccessibleInterval<FloatType> result = clij.pullRAI(resultBuffer);
			ImgLib2Assert.assertImageEqualsRealType(expected, result, 0);
		}
	}

	private CLIJView image(float content) {
		Img<FloatType> image = ArrayImgs.floats(new float[]{ content }, 1, 1);
		return CLIJView.wrap(clij.push(image));
	}

	private float getValue(CLIJView view) {
		RandomAccessibleInterval<FloatType> rai = clij.pullRAI(view.buffer());
		return Views.interval(rai, view.interval()).firstElement().getRealFloat();
	}

	private float getValue(ClearCLBuffer buffer) {
		RandomAccessibleInterval<FloatType> rai = clij.pullRAI(buffer);
		return Views.iterable(rai).firstElement().getRealFloat();
	}

	@After
	public void after() {
		autoClose.close();
		clij.close();
	}
}
