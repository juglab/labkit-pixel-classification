
package sc.fiji.labkit.pixel_classification.gpu.algorithms;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.gpu.api.AbstractGpuTest;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPixelWiseOperation;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPool;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class GpuEigenvaluesTest extends AbstractGpuTest {

	@Test
	public void testCalculate2d() {
		GpuView xx = image(1);
		GpuView xy = image(2);
		GpuView yy = image(3);
		GpuView eigenvalue1 = image(0);
		GpuView eigenvalue2 = image(0);
		GpuEigenvalues.symmetric2d(gpu, xx, xy, yy, eigenvalue1, eigenvalue2);
		assertEquals(2 + Math.sqrt(5), getValue(eigenvalue1), 0.0001);
		assertEquals(2 - Math.sqrt(5), getValue(eigenvalue2), 0.0001);
	}

	@Test
	public void testCalculate3d() {
		GpuView xx = image(1);
		GpuView xy = image(2);
		GpuView xz = image(3);
		GpuView yy = image(4);
		GpuView yz = image(5);
		GpuView zz = image(6);
		GpuView eigenvalue1 = image(0);
		GpuView eigenvalue2 = image(0);
		GpuView eigenvalue3 = image(0);
		GpuEigenvalues.symmetric3d(gpu, xx, xy, xz, yy, yz, zz, eigenvalue1, eigenvalue2, eigenvalue3);
		assertEquals(11.345, getValue(eigenvalue1), 0.001);
		assertEquals(0.171, getValue(eigenvalue2), 0.001);
		assertEquals(-0.516, getValue(eigenvalue3), 0.001);
	}

	@Test
	public void testCubicEquation() {
		assertArrayEquals(new float[] { -1, 7, 8 }, solveCubicEquation(56, 41, -14), 0.0001f);
		assertArrayEquals(new float[] { 0, 0, 0 }, solveCubicEquation(0, 0, 0), 0.0001f);
	}

	private float[] solveCubicEquation(float b0, float b1, float b2) {
		long[] dims = { 1, 1 };
		GpuImage x0 = gpu.create(dims, NativeTypeEnum.Float);
		GpuImage x1 = gpu.create(dims, NativeTypeEnum.Float);
		GpuImage x2 = gpu.create(dims, NativeTypeEnum.Float);
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("b0", b0)
			.addInput("b1", b1)
			.addInput("b2", b2)
			.addOutput("x0", x0)
			.addOutput("x1", x1)
			.addOutput("x2", x2)
			.forEachPixel("double x[3];" +
				"solve_cubic_equation(b0, b1, b2, x);" +
				"x0 = x[0]; x1 = x[1]; x2 = x[2];");
		return new float[] { getValue(x0), getValue(x1), getValue(x2) };
	}

	@Test
	public void testPrecision2() {
		Random random = new Random(42);
		int n = 10000;
		Img<FloatType> expected = ArrayImgs.floats(3, n);
		Cursor<FloatType> cursor = expected.cursor();
		for (int i = 0; i < n; i++) {
			double[] expectedX = new double[] { random.nextDouble(), random.nextDouble(), random
				.nextDouble() };
			Arrays.sort(expectedX);
			cursor.next().setReal(expectedX[0]);
			cursor.next().setReal(expectedX[1]);
			cursor.next().setReal(expectedX[2]);
		}
		Img<FloatType> zeros = ArrayImgs.floats(1, n);
		GpuImage expectedBuffer = gpu.push(expected);
		GpuImage zeroBuffer = gpu.push(zeros);
		GpuImage resultBuffer = gpu.create(new long[] { 3, n }, NativeTypeEnum.Float);
		GpuView xx = GpuViews.crop(expectedBuffer, Intervals.createMinSize(0, 0, 1, n));
		GpuView yy = GpuViews.crop(expectedBuffer, Intervals.createMinSize(1, 0, 1, n));
		GpuView zz = GpuViews.crop(expectedBuffer, Intervals.createMinSize(2, 0, 1, n));
		GpuView zero = GpuViews.wrap(zeroBuffer);
		GpuView e1 = GpuViews.crop(resultBuffer, Intervals.createMinSize(0, 0, 1, n));
		GpuView e2 = GpuViews.crop(resultBuffer, Intervals.createMinSize(1, 0, 1, n));
		GpuView e3 = GpuViews.crop(resultBuffer, Intervals.createMinSize(2, 0, 1, n));
		GpuEigenvalues.symmetric3d(gpu, yy, zero, zero, xx, zero, zz, e3, e2, e1);
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(resultBuffer);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0);
	}

	private GpuView image(float content) {
		Img<FloatType> image = ArrayImgs.floats(new float[] { content }, 1, 1);
		return GpuViews.wrap(gpu.push(image));
	}

	private float getValue(GpuView view) {
		return getValue(GpuViews.asGpuImage(gpu, view));
	}

	private float getValue(GpuImage buffer) {
		RandomAccessibleInterval<FloatType> rai = gpu.pullRAI(buffer);
		return Views.iterable(rai).firstElement().getRealFloat();
	}
}
