
package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.Test;

/**
 * Tests {@link SumFilter}.
 */
public class SumFilterTest {

	@Test
	public void test() {
		Img<IntType> input = ArrayImgs.ints(new int[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		Img<IntType> output = ArrayImgs.ints(5, 5);
		Img<IntType> expected = ArrayImgs.ints(new int[] {
			0, 0, 0, 0, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		SumFilter.convolution(3, 3).process(Views.extendBorder(input), output);
		ImgLib2Assert.assertImageEquals(expected, output);
	}
}
