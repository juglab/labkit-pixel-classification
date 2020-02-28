
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

/**
 * Tests {@link ClijLoopBuilder}.
 */
public class ClijLoopBuilderTest {

	private final CLIJ2 clij = CLIJ2.getInstance();

	@Test
	public void testAdd() {
		long[] dims = { 2, 2 };
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, dims));
		ClearCLBuffer b = clij.push(ArrayImgs.floats(new float[] { 5, 6, 7, 8 }, dims));
		ClearCLBuffer c = clij.create(dims);
		add(a, b, c);
		RandomAccessibleInterval<RealType<?>> result = clij.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 6, 8, 10, 12 },
			dims);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	private void add(ClearCLBuffer a, ClearCLBuffer b, ClearCLBuffer dst) {
		ClijLoopBuilder.clij(clij)
			.setImage("a", a)
			.setImage("b", b)
			.setImage("c", dst)
			.forEachPixel("c = a + b");
	}

	@Test
	public void testSingleImageOperation() {
		long[] dims = { 2, 2 };
		ClearCLBuffer c = clij.create(dims, NativeTypeEnum.Float);
		ClijLoopBuilder.clij(clij)
			.setImage("output", c)
			.forEachPixel("output = 2.0");
		RandomAccessibleInterval<RealType<?>> result = clij.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 2, 2, 2, 2 }, 2,
			2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test
	public void testTwoImageOperation() {
		long[] dims = { 2, 2 };
		ClearCLBuffer c = clij.create(dims, NativeTypeEnum.Byte);
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, dims));
		ClijLoopBuilder.clij(clij)
			.setImage("in", a)
			.setImage("out", c)
			.forEachPixel("out = 2.0 * in");
		RandomAccessibleInterval<RealType<?>> result = clij.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 2, 4, 6, 8 },
			dims);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test
	public void testMultipleOutputs() {
		long[] dims = { 2, 2 };
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, dims));
		ClearCLBuffer b = clij.create(dims);
		ClearCLBuffer c = clij.create(dims);
		ClijLoopBuilder.clij(clij)
			.setImage("a", a)
			.setImage("b", b)
			.setImage("c", c)
			.forEachPixel("b = 2 * a; c = a + b");
		RandomAccessibleInterval<RealType<?>> resultB = clij.pullRAI(b);
		RandomAccessibleInterval<RealType<?>> resultC = clij.pullRAI(c);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 2, 4, 6, 8 }, dims),
			resultB, 0.0);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 3, 6, 9, 12 }, dims),
			resultC, 0.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMismatchingDimensions() {
		ClearCLBuffer c = clij.create(new long[] { 10, 10 });
		ClearCLBuffer b = clij.create(new long[] { 10, 11 });
		ClijLoopBuilder.clij(clij)
			.setImage("c", c)
			.setImage("b", b)
			.forEachPixel("b = c");
	}
}
