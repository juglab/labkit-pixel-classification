
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link CLIJCopy}
 */
public class CLIJCopyTest {

	private CLIJ2 clij;

	@Before
	public void before() {
		clij = CLIJ2.getInstance();
	}

	@After
	public void after() {
		clij.close();
	}

	@Test
	public void testCopy() {
		ClearCLBuffer source = clij.push(ArrayImgs.floats(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 3,
			3));
		ClearCLBuffer destination = clij.create(new long[] { 3, 3 }, NativeTypeEnum.Float);
		CLIJView sourceView = CLIJView.interval(source, Intervals.createMinSize(0, 1, 2, 2));
		CLIJView destinationView = CLIJView.interval(destination, Intervals.createMinSize(1, 0, 2, 2));
		CLIJCopy.copy(clij, sourceView, destinationView);
		RandomAccessibleInterval<FloatType> result = clij.pullRAI(destination);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 0, 4, 5, 0, 7, 8,
			0, 0, 0 }, 3, 3);
		ImgLib2Assert.assertImageEquals(expected, result);
	}
}
