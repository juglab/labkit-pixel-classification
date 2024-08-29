/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.utils;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import sc.fiji.labkit.pixel_classification.utils.ToString;
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
