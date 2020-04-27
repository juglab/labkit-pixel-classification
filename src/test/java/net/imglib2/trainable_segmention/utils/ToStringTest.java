
package net.imglib2.trainable_segmention.utils;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.trainable_segmention.utils.ToString;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToStringTest {

	@Test
	public void test1D() {
		RandomAccessibleInterval<IntType> image = ArrayImgs.ints(new int[] { 1, 2, 3 }, 3);
		String string = ToString.toString(image);
		assertEquals("{1, 2, 3}", string);
	}

	@Test
	public void test2D() {
		RandomAccessibleInterval<IntType> image = ArrayImgs.ints(new int[] { 1, 2, 3, 4, 5, 6 }, 3, 2);
		String string = ToString.toString(image);
		assertEquals("{\n\t{1, 2, 3},\n\t{4, 5, 6}\n}", string);
	}

	@Test
	public void test3D() {
		RandomAccessibleInterval<IntType> image = ArrayImgs.ints(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 },
			2, 2, 2);
		String string = ToString.toString(image);
		assertEquals("{\n\t{\n\t\t{1, 2},\n\t\t{3, 4}\n\t},\n\t{\n\t\t{5, 6},\n\t\t{7, 8}\n\t}\n}",
			string);
	}
}
