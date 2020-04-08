
package net.imglib2.trainable_segmention.gpu.api;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Test {@link GpuApi}.
 */
public class GpuApiTest {

	private final GpuApi gpu = GpuApi.getInstance();

	@After
	public void after() {
		gpu.close();
	}

	@Test
	public void testCreate() {
		GpuImage image = gpu.create(new long[] { 10, 20 }, 3, NativeTypeEnum.Byte);
		assertEquals(10, image.getWidth());
		assertEquals(20, image.getHeight());
		assertEquals(1, image.getDepth());
		assertEquals(3, image.getNumberOfChannels());
		assertEquals(NativeTypeEnum.Byte, image.getNativeType());
	}

	@Test
	public void testBufferReuse() {
		ClearCLBuffer aBuffer, bBuffer;
		try (GpuApi scope = gpu.subScope()) {
			GpuImage a = scope.create(new long[] { 10, 10 }, NativeTypeEnum.Float);
			aBuffer = a.clearCLBuffer();
		}
		try (GpuApi scope = gpu.subScope()) {
			GpuImage b = scope.create(new long[] { 10, 10 }, NativeTypeEnum.Float);
			bBuffer = b.clearCLBuffer();
		}
		assertSame(aBuffer, bBuffer);
	}

	@Test
	public void testPushAndPull() {
		RandomAccessibleInterval<FloatType> image = ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6 },
			2, 3);
		GpuImage gpuImage = gpu.push(image);
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(gpuImage);
		ImgLib2Assert.assertImageEquals(image, result);
	}

	@Test
	public void testPushAndPullMultiChannel() {
		RandomAccessibleInterval<FloatType> image = ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6 },
			2, 1, 3);
		GpuImage gpuImage = gpu.pushMultiChannel(image);
		assertArrayEquals(new long[] { 2, 1 }, gpuImage.getDimensions());
		assertEquals(3, gpuImage.getNumberOfChannels());
		RandomAccessibleInterval<FloatType> result = gpu.pullRAIMultiChannel(gpuImage);
		ImgLib2Assert.assertImageEquals(image, result);
	}
}
