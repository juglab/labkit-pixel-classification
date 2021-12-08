/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats;

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
