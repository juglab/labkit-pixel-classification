
package sc.fiji.labkit.pixel_classification.gpu.api;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link GpuCopy}
 */
public class GpuCopyTest extends AbstractGpuTest {

	@Test
	public void testCopy() {
		GpuImage source = gpu.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 3, 3));
		GpuImage destination = gpu.push(ArrayImgs.floats(new float[9], 3, 3));
		GpuView sourceView = GpuViews.crop(source, Intervals.createMinSize(0, 1, 2, 2));
		GpuView destinationView = GpuViews.crop(destination, Intervals.createMinSize(1, 0, 2, 2));
		GpuCopy.copyFromTo(gpu, sourceView, destinationView);
		RandomAccessibleInterval<FloatType> result = gpu.pullRAI(destination);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 0, 4, 5, 0, 7, 8,
			0, 0, 0 }, 3, 3);
		ImgLib2Assert.assertImageEquals(expected, result);
	}

	@Test
	public void testCopyToRai() {
		GpuImage source = gpu.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 3, 3));
		RandomAccessibleInterval<FloatType> target = ArrayImgs.floats(3, 3);
		GpuCopy.copyFromTo(source, target);
	}
}
