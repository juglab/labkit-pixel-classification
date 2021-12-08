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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.lipschitz;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.function.DoubleBinaryOperator;

@Deprecated
public class SingleLipschitzFeatureTest {

	@Test
	public void test() {
		Img<FloatType> image = dirac();
		Img<FloatType> result = ArrayImgs.floats(5, 5);
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.addFeature(SingleFeatures.lipschitz(0.1, 0))
			.build();
		calculator.apply(image, Views.addDimension(result, 0, 0));
		LoopBuilder.setImages(result).forEachPixel(x -> x.sub(new FloatType(255)));
		Img<FloatType> expected = createImage((x, y) -> (x == 2) && (y == 2) ? 0 : -1 + 0.1 * Math.sqrt(
			Math.pow(x - 2, 2) + Math.pow(y - 2, 2)));
		Utils.assertImagesEqual(35, expected, result);
	}

	@Test
	public void testOpening() {
		Img<FloatType> image = dirac();
		Img<FloatType> expected = createImage((x, y) -> 1 - Math.sqrt(Math.pow(0.1 * (x - 2), 2) + Math
			.pow(0.2 * (y - 2), 2)));
		ConeMorphology.performConeOperation(ConeMorphology.Operation.DILATION, image, new double[] {
			0.1, 0.2 });
		Utils.assertImagesEqual(35, expected, image);
	}

	@Test
	public void testClosing() {
		Img<FloatType> image = negate(dirac());
		Img<FloatType> expected = createImage((x, y) -> -1 + Math.sqrt(Math.pow(0.1 * (x - 2), 2) + Math
			.pow(0.2 * (y - 2), 2)));
		ConeMorphology.performConeOperation(ConeMorphology.Operation.EROSION, image, new double[] { 0.1,
			0.2 });
		Utils.assertImagesEqual(35, expected, image);
	}

	// -- Helper methods --

	private static Img<FloatType> dirac() {
		return createImage((x, y) -> (x == 2) && (y == 2) ? 1 : 0);
	}

	private static Img<FloatType> createImage(DoubleBinaryOperator function) {
		Img<FloatType> expected = ArrayImgs.floats(5, 5);
		Cursor<FloatType> cursor = expected.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			double x = cursor.getDoublePosition(0);
			double y = cursor.getDoublePosition(1);
			cursor.get().setReal(function.applyAsDouble(x, y));
		}
		return expected;
	}

	private Img<FloatType> negate(Img<FloatType> dirac) {
		dirac.forEach(x -> x.mul(-1));
		return dirac;
	}

}
