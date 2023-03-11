/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

package preview.net.imglib2.algorithm.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Localizables;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HyperEllipsoidNeighborhoodTest {

	@Test
	public void test() {
		double[] radii = { 3.5, 2.5 };
		Img<BitType> expected = drawExpectedEllipsoid(radii);
		Img<BitType> result = ArrayImgs.bits(9, 9);
		HyperEllipsoidNeighborhood<BitType> neighborhood = new HyperEllipsoidNeighborhood<>(new long[] {
			4, 4 }, radii, result.randomAccess());
		neighborhood.forEach(BitType::setOne);
		ImgLib2Assert.assertImageEquals(expected, result);
	}

	@Test
	public void testSphere() {
		Shape sphere = new HyperSphereShape(5);
		Shape ellipsoid = new HyperEllipsoidShape(new double[] { 5, 5, 5 });
		RandomAccessible<Localizable> positions = Localizables.randomAccessible(3);
		Neighborhood<Localizable> rectangleNeighborHood = sphere.neighborhoodsRandomAccessible(
			positions).randomAccess().get();
		Neighborhood<Localizable> ellipsoidNeighborHood = ellipsoid.neighborhoodsRandomAccessible(
			positions).randomAccess().get();
		assertCursorsEqual(rectangleNeighborHood.cursor(), ellipsoidNeighborHood.cursor());
	}

	private void assertCursorsEqual(Cursor<Localizable> e, Cursor<Localizable> a) {
		while (true) {
			boolean eHasNext = e.hasNext();
			boolean aHasNext = a.hasNext();
			assertEquals(eHasNext, aHasNext);
			if (!(aHasNext && eHasNext))
				return;
			long[] expectedCoordinates = Localizables.asLongArray(e.next());
			long[] actualCoordinates = Localizables.asLongArray(a.next());
			assertArrayEquals("Coordinates differ, expected " + Arrays.toString(expectedCoordinates) +
				" actual " + Arrays.toString(actualCoordinates),
				expectedCoordinates, actualCoordinates);
		}
	}

	private Img<BitType> drawExpectedEllipsoid(double[] radii) {
		return fillImage(ArrayImgs.bits(9, 9), (x, y) -> Math.pow((x - 4) / radii[0], 2) + Math.pow((y -
			4) / radii[1], 2) <= 1);
	}

	private Img<BitType> fillImage(Img<BitType> image, BiFunction<Double, Double, Boolean> content) {
		Cursor<BitType> cursor = image.cursor();
		while (cursor.hasNext()) {
			BitType pixel = cursor.next();
			pixel.set(content.apply(cursor.getDoublePosition(0), cursor.getDoublePosition(1)));
		}
		return image;
	}
}
