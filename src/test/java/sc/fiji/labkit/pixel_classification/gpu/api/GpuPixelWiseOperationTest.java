
package sc.fiji.labkit.pixel_classification.gpu.api;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.After;
import org.junit.Test;

/**
 * Tests {@link GpuPixelWiseOperation}.
 */
public class GpuPixelWiseOperationTest extends AbstractGpuTest {

	@Test
	public void testAdd() {
		long[] dims = { 2, 2 };
		GpuImage a = gpu.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, dims));
		GpuImage b = gpu.push(ArrayImgs.floats(new float[] { 5, 6, 7, 8 }, dims));
		GpuImage c = gpu.create(dims, NativeTypeEnum.Float);
		add(a, b, c);
		RandomAccessibleInterval<RealType<?>> result = gpu.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 6, 8, 10, 12 },
			dims);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	private void add(GpuImage a, GpuImage b, GpuImage dst) {
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("a", a)
			.addInput("b", b)
			.addOutput("c", dst)
			.forEachPixel("c = a + b");
	}

	@Test
	public void testSingleImageOperation() {
		long[] dims = { 2, 2 };
		GpuImage c = gpu.create(dims, NativeTypeEnum.Float);
		GpuPixelWiseOperation.gpu(gpu)
			.addOutput("output", c)
			.forEachPixel("output = 2.0");
		RandomAccessibleInterval<RealType<?>> result = gpu.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 2, 2, 2, 2 }, 2,
			2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test
	public void testTwoImageOperation() {
		long[] dims = { 2, 2 };
		GpuImage c = gpu.create(dims, NativeTypeEnum.Byte);
		GpuImage a = gpu.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, dims));
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("in", a)
			.addOutput("out", c)
			.forEachPixel("out = 2.0 * in");
		RandomAccessibleInterval<RealType<?>> result = gpu.pullRAI(c);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 2, 4, 6, 8 },
			dims);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test
	public void testMultipleOutputs() {
		long[] dims = { 2, 2 };
		GpuImage a = gpu.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4 }, dims));
		GpuImage b = gpu.create(dims, NativeTypeEnum.Float);
		GpuImage c = gpu.create(dims, NativeTypeEnum.Float);
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("a", a)
			.addOutput("b", b)
			.addOutput("c", c)
			.forEachPixel("b = 2 * a; c = a + b");
		RandomAccessibleInterval<RealType<?>> resultB = gpu.pullRAI(b);
		RandomAccessibleInterval<RealType<?>> resultC = gpu.pullRAI(c);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 2, 4, 6, 8 }, dims),
			resultB, 0.0);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 3, 6, 9, 12 }, dims),
			resultC, 0.0);
	}

	@Test
	public void testFourImages() {
		GpuImage a = gpu.push(ArrayImgs.floats(new float[] { 1 }, 1, 1));
		GpuImage b = gpu.push(ArrayImgs.floats(new float[] { 2 }, 1, 1));
		GpuImage c = gpu.push(ArrayImgs.floats(new float[] { 3 }, 1, 1));
		GpuImage d = gpu.push(ArrayImgs.floats(new float[] { 0 }, 1, 1));
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("a", a)
			.addInput("b", b)
			.addInput("c", c)
			.addOutput("d", d)
			.forEachPixel("d = a + b + c");
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 6 }, 1, 1), result, 0.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMismatchingDimensions() {
		GpuImage c = gpu.create(new long[] { 10, 10 }, NativeTypeEnum.Float);
		GpuImage b = gpu.create(new long[] { 10, 11 }, NativeTypeEnum.Float);
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("c", c)
			.addInput("b", b)
			.forEachPixel("b = c");
	}

	@Test
	public void testVariable() {
		GpuImage d = gpu.push(ArrayImgs.floats(new float[] { 0 }, 1, 1));
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("a", 42)
			.addOutput("d", d)
			.forEachPixel("d = a");
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 42 }, 1, 1), result,
			0.0);
	}

	@Test
	public void testFloatVariable() {
		GpuImage d = gpu.push(ArrayImgs.floats(new float[] { 0 }, 1, 1));
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("a", 42f)
			.addOutput("d", d)
			.forEachPixel("d = a");
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 42 }, 1, 1), result,
			0.0);
	}

	@Test
	public void testCLIJViewInput() {
		GpuView a = GpuViews.crop(gpu.push(ArrayImgs.floats(new float[] { 0, 0, 0, 42 }, 2, 2)),
			Intervals.createMinSize(1, 1, 1, 1));
		GpuImage d = gpu.push(ArrayImgs.floats(new float[] { 0 }, 1, 1));
		GpuPixelWiseOperation.gpu(gpu)
			.addInput("a", a)
			.addOutput("d", d)
			.forEachPixel("d = a");
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(d);
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 42 }, 1, 1), result,
			0.0);
	}

	@Test
	public void testCLIJViewOutput() {
		GpuImage image = gpu.push(ArrayImgs.floats(new float[] { 0, 0, 0, 0 }, 2, 2));
		GpuView a = GpuViews.crop(image, Intervals.createMinSize(1, 1, 1, 1));
		GpuPixelWiseOperation.gpu(gpu)
			.addOutput("a", a)
			.forEachPixel("a = 42");
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(a.source());
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 0, 0, 0, 42 }, 2, 2),
			result, 0.0);
	}

	@Test
	public void testDifference() {
		try (
			GpuImage a = gpu.push(ArrayImgs.floats(new float[] { 1, 7, 8 }, 3, 1));
			GpuImage o = gpu.create(new long[] { 2, 1 }, NativeTypeEnum.Float);)
		{
			GpuPixelWiseOperation.gpu(gpu)
				.addInput("a", GpuViews.crop(a, Intervals.createMinSize(1, 0, 2, 1)))
				.addInput("b", GpuViews.crop(a, Intervals.createMinSize(0, 0, 2, 1)))
				.addOutput("c", o)
				.forEachPixel("c = a - b");
			RandomAccessibleInterval<FloatType> result = gpu.pullRAI(o);
			ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[] { 6, 1 }, 2, 1), result,
				0.0);
		}
	}

	@Test
	public void test3d() {
		try (
			GpuImage a = gpu.push(create3dImage(1));
			GpuImage b = gpu.push(create3dImage(2));
			GpuImage r = gpu.create(new long[] { 21, 21, 21 }, NativeTypeEnum.Float);)
		{
			GpuPixelWiseOperation.gpu(gpu)
				.addInput("a", GpuViews.wrap(a))
				.addInput("b", GpuViews.wrap(b))
				.addOutput("r", GpuViews.wrap(r))
				.forEachPixel("r = a - b");
			ImgLib2Assert.assertImageEqualsRealType(create3dImage(-1), gpu.pullRAI(r), 0);
		}

	}

	private Img<FloatType> create3dImage(float factor) {
		Img<FloatType> image = ArrayImgs.floats(21, 21, 21);
		int i = 0;
		for (FloatType pixel : image)
			pixel.setReal((++i) * factor);
		return image;
	}

	@Test
	public void testSameImage() {
		try (
			GpuImage a = gpu.push(ArrayImgs.floats(new float[] { 1 }, 1, 1));
			GpuImage c = gpu.create(new long[] { 1, 1 }, NativeTypeEnum.Float);)
		{
			GpuPixelWiseOperation.gpu(gpu)
				.addInput("a", a)
				.addInput("b", a)
				.addOutput("c", c)
				.forEachPixel("c = a + b");
			RandomAccessibleInterval<FloatType> result = gpu.pullRAI(c);
			RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 2 }, 1, 1);
			ImgLib2Assert.assertImageEqualsRealType(expected, result, 0);
		}
	}

	@Test
	public void testSameBufferCLIJView() {
		try (
			GpuImage a = gpu.push(ArrayImgs.floats(new float[] { 1, 4 }, 2, 1));
			GpuImage c = gpu.create(new long[] { 1, 1 }, NativeTypeEnum.Float);)
		{
			GpuPixelWiseOperation.gpu(gpu)
				.addInput("a", GpuViews.crop(a, Intervals.createMinSize(0, 0, 1, 1)))
				.addInput("b", GpuViews.crop(a, Intervals.createMinSize(1, 0, 1, 1)))
				.addOutput("c", c)
				.forEachPixel("c = a + b");
			RandomAccessibleInterval<FloatType> result = gpu.pullRAI(c);
			RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 5 }, 1, 1);
			ImgLib2Assert.assertImageEqualsRealType(expected, result, 0);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalVariableName() {
		GpuPixelWiseOperation.gpu(gpu).addInput("float", 7).forEachPixel("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalVariableName2() {
		try (GpuImage image = gpu.create(new long[] { 1, 1 }, NativeTypeEnum.Float)) {
			GpuPixelWiseOperation.gpu(gpu).addInput("float", image).forEachPixel("");
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVariableNameClash() {
		try (GpuImage image = gpu.create(new long[] { 1, 1 }, NativeTypeEnum.Float)) {
			GpuPixelWiseOperation.gpu(gpu).addInput("coordinate_x", image).forEachPixel(
				"coordinate_x = 8");
		}
	}
}
