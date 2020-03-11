package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;

/**
 * Tests {@link CLIJLoopBuilder}.
 */
public class CLIJLoopBuilderTest {

	private final CLIJ2 clij = CLIJ2.getInstance();

	@Test
	public void testAdd() {
		long[] dims = {2, 2};
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[]{1, 2, 3, 4}, dims));
		ClearCLBuffer b = clij.push(ArrayImgs.floats(new float[]{5, 6, 7, 8}, dims));
		ClearCLBuffer c = clij.create(dims);
		add(a, b, c);
		RandomAccessibleInterval<RealType<?>> result = clij.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[]{6, 8, 10, 12}, dims);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	private void add(ClearCLBuffer a, ClearCLBuffer b, ClearCLBuffer dst) {
		CLIJLoopBuilder.clij(clij)
				.addInput("a", a)
				.addInput("b", b)
				.addOutput("c", dst)
				.forEachPixel("c = a + b");
	}

	@Test
	public void testSingleImageOperation() {
		long[] dims = {2, 2};
		ClearCLBuffer c = clij.create(dims, NativeTypeEnum.Float);
		CLIJLoopBuilder.clij(clij)
				.addOutput("output", c)
				.forEachPixel("output = 2.0");
		RandomAccessibleInterval<RealType<?>> result = clij.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[]{2, 2, 2, 2}, 2, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test
	public void testTwoImageOperation() {
		long[] dims = {2, 2};
		ClearCLBuffer c = clij.create(dims, NativeTypeEnum.Byte);
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[]{1, 2, 3, 4}, dims));
		CLIJLoopBuilder.clij(clij)
				.addInput("in", a)
				.addOutput("out", c)
				.forEachPixel("out = 2.0 * in");
		RandomAccessibleInterval<RealType<?>> result = clij.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[]{2, 4, 6, 8}, dims);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test
	public void testMultipleOutputs() {
		long[] dims = {2, 2};
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[]{1, 2, 3, 4}, dims));
		ClearCLBuffer b = clij.create(dims);
		ClearCLBuffer c = clij.create(dims);
		CLIJLoopBuilder.clij(clij)
				.addInput("a", a)
				.addOutput("b", b)
				.addOutput("c", c)
				.forEachPixel("b = 2 * a; c = a + b");
		RandomAccessibleInterval<RealType<?>> resultB = clij.pullRAI(b);
		RandomAccessibleInterval<RealType<?>> resultC = clij.pullRAI(c);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{2, 4, 6, 8}, dims), resultB, 0.0);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{3, 6, 9, 12}, dims), resultC, 0.0);
	}

	@Test
	public void testFourImages() {
		ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[]{1}, 1, 1));
		ClearCLBuffer b = clij.push(ArrayImgs.floats(new float[]{2}, 1, 1));
		ClearCLBuffer c = clij.push(ArrayImgs.floats(new float[]{3}, 1, 1));
		ClearCLBuffer d = clij.push(ArrayImgs.floats(new float[]{0}, 1, 1));
		CLIJLoopBuilder.clij(clij)
				.addInput("a", a)
				.addInput("b", b)
				.addInput("c", c)
				.addOutput("d", d)
				.forEachPixel("d = a + b + c");
		RandomAccessibleInterval<FloatType> result = clij.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{6}, 1, 1), result, 0.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMismatchingDimensions() {
		ClearCLBuffer c = clij.create(new long[]{10, 10});
		ClearCLBuffer b = clij.create(new long[]{10, 11});
		CLIJLoopBuilder.clij(clij)
				.addInput("c", c)
				.addInput("b", b)
				.forEachPixel("b = c");
	}

	@Test
	public void testVariable() {
		ClearCLBuffer d = clij.push(ArrayImgs.floats(new float[]{0}, 1, 1));
		CLIJLoopBuilder.clij(clij)
				.addInput("a", 42)
				.addOutput("d", d)
				.forEachPixel("d = a");
		RandomAccessibleInterval<FloatType> result = clij.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{42}, 1, 1), result, 0.0);
	}

	@Test
	public void testFloatVariable() {
		ClearCLBuffer d = clij.push(ArrayImgs.floats(new float[]{0}, 1, 1));
		CLIJLoopBuilder.clij(clij)
				.addInput("a", 42f)
				.addOutput("d", d)
				.forEachPixel("d = a");
		RandomAccessibleInterval<FloatType> result = clij.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{42}, 1, 1), result, 0.0);
	}

	@Test
	public void testCLIJViewInput() {
		CLIJView a = CLIJView.interval(clij.push(ArrayImgs.floats(new float[]{0, 0, 0, 42}, 2, 2)), Intervals.createMinSize(1,1,1,1));
		ClearCLBuffer d = clij.push(ArrayImgs.floats(new float[]{0}, 1, 1));
		CLIJLoopBuilder.clij(clij)
				.addInput("a", a)
				.addOutput("d", d)
				.forEachPixel("d = a");
		RandomAccessibleInterval<FloatType> result = clij.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{42}, 1, 1), result, 0.0);
	}

	@Test
	public void testCLIJViewOutput() {
		CLIJView a = CLIJView.interval(clij.create(new long[]{2, 2}, NativeTypeEnum.Float), Intervals.createMinSize(1,1,1,1));
		CLIJLoopBuilder.clij(clij)
				.addOutput("a", a)
				.forEachPixel("a = 42");
		RandomAccessibleInterval<FloatType> result = clij.pullRAI(a.buffer());
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{0,0,0,42}, 2, 2), result, 0.0);
	}

	@Test
	public void testDifference() {
		try (
			ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[]{1,7,8}, 3, 1));
			ClearCLBuffer o = clij.create(new long[]{2, 1}, NativeTypeEnum.Float);
		) {
			CLIJLoopBuilder.clij(clij)
					.addInput("a", CLIJView.interval(a, Intervals.createMinSize(1, 0, 2, 1)))
					.addInput("b", CLIJView.interval(a, Intervals.createMinSize(0, 0, 2, 1)))
					.addOutput("c", o)
					.forEachPixel("c = a - b");
			RandomAccessibleInterval<FloatType> result = clij.pullRAI(o);
			ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{6, 1}, 2, 1), result, 0.0);
		}
	}

	@Test
	public void testAdd3d() {
		try(
			ClearCLBuffer a = clij.push(create3dImage(1));
			ClearCLBuffer b = clij.push(create3dImage(2));
			ClearCLBuffer r = clij.create(new long[]{21, 21, 21});
		) {
			CLIJLoopBuilder.clij(clij)
					.addInput("a", CLIJView.wrap(a))
					.addInput("b", CLIJView.wrap(b))
					.addOutput("r", CLIJView.wrap(r))
					.forEachPixel("r = a - b");
			ImgLib2Assert.assertImageEqualsRealType(create3dImage(-1), clij.pullRAI(r), 0);
		}

	}

	@Test
	public void testSameImage() {
		try (
			ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[]{1}, 1, 1));
			ClearCLBuffer c = clij.create(new long[]{1,  1}, NativeTypeEnum.Float);
		) {
			CLIJLoopBuilder.clij(clij)
					.addInput("a", a)
					.addInput( "b", a)
					.addOutput("c", c)
					.forEachPixel("c = a + b");
			RandomAccessibleInterval<FloatType> result = clij.pullRAI(c);
			RandomAccessibleInterval<FloatType> expected = 	 ArrayImgs.floats(new float[]{2}, 1, 1);
			ImgLib2Assert.assertImageEqualsRealType(expected, result, 0);
		}
	}

	@Test
	public void testSameBufferCLIJView() {
		try (
			ClearCLBuffer a = clij.push(ArrayImgs.floats(new float[]{1, 4}, 2, 1));
			ClearCLBuffer c = clij.create(new long[]{1, 1}, NativeTypeEnum.Float);
		) {
			CLIJLoopBuilder.clij(clij)
					.addInput("a", CLIJView.interval(a, Intervals.createMinSize(0,0,1,1)))
					.addInput( "b", CLIJView.interval(a, Intervals.createMinSize(1,0,1,1)))
					.addOutput("c", c)
					.forEachPixel("c = a + b");
			RandomAccessibleInterval<FloatType> result = clij.pullRAI(c);
			RandomAccessibleInterval<FloatType> expected = 	 ArrayImgs.floats(new float[]{5}, 1, 1);
			ImgLib2Assert.assertImageEqualsRealType(expected, result, 0);
		}
	}

	private Img<FloatType> create3dImage(float factor) {
		Img<FloatType> image = ArrayImgs.floats(21, 21, 21);
		int i = 0;
		for(FloatType pixel : image)
			pixel.setReal((++i) * factor);
		return image;
	}
}
