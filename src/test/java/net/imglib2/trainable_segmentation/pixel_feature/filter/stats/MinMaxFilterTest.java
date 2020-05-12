
package net.imglib2.trainable_segmentation.pixel_feature.filter.stats;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import org.junit.Test;

/**
 * Tests {@link MinMaxFilter}.
 */
public class MinMaxFilterTest {

	@Test
	public void testIncreasing() {
		Img<DoubleType> in = ArrayImgs.doubles(new double[] { 1, 2, 3, 4, 5 }, 5);
		Img<DoubleType> out = ArrayImgs.doubles(4);
		Img<DoubleType> expected = ArrayImgs.doubles(new double[] { 2, 3, 4, 5 }, 4);
		new MinMaxFilter.MinMaxConvolver(MinMaxFilter.Operation.MAX, 2, in.randomAccess(), out
			.randomAccess(), 0, out.size()).run();
		ImgLib2Assert.assertImageEquals(expected, out);
	}

	@Test
	public void testDecreasing() {
		Img<DoubleType> in = ArrayImgs.doubles(new double[] { 5, 4, 3, 2, 1 }, 5);
		Img<DoubleType> out = ArrayImgs.doubles(4);
		Img<DoubleType> expected = ArrayImgs.doubles(new double[] { 5, 4, 3, 2 }, 4);
		new MinMaxFilter.MinMaxConvolver(MinMaxFilter.Operation.MAX, 2, in.randomAccess(), out
			.randomAccess(), 0, out.size()).run();
		ImgLib2Assert.assertImageEquals(expected, out);
	}

	@Test
	public void testMaxFilter() {
		Img<DoubleType> in = ArrayImgs.doubles(new double[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		Img<DoubleType> out = ArrayImgs.doubles(5, 5);
		Img<DoubleType> expected = ArrayImgs.doubles(new double[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		MinMaxFilter.maxFilter(3, 2).process(Views.extendBorder(in), out);
		ImgLib2Assert.assertImageEquals(expected, out);
	}

	@Test
	public void testMinFilter() {
		Img<DoubleType> in = ArrayImgs.doubles(new double[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, -1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		Img<DoubleType> out = ArrayImgs.doubles(5, 5);
		Img<DoubleType> expected = ArrayImgs.doubles(new double[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, -1, -1, -1, 0,
			0, -1, -1, -1, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		MinMaxFilter.minFilter(3, 2).process(Views.extendBorder(in), out);
		ImgLib2Assert.assertImageEquals(expected, out);
	}
}
