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

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.loops.LoopBuilder;

import java.util.function.BiConsumer;
import java.util.stream.LongStream;

class ConeMorphology {

	static <T extends RealType<T>> void performConeOperation(Operation operation,
		RandomAccessibleInterval<T> inOut, double[] slope)
	{
		for (Localizable location : neighborhood(inOut.numDimensions())) {
			oneRun(slope, Localizables.asLongArray(location), inOut, operation);
		}
	}

	private static Iterable<Localizable> neighborhood(int n) {
		Shape shape = new RectangleShape(1, true);
		RandomAccess<Neighborhood<Localizable>> ra = shape.neighborhoodsRandomAccessible(Localizables
			.randomAccessible(n)).randomAccess();
		ra.setPosition(RevampUtils.nCopies(ra.numDimensions(), 0L));
		return ra.get();
	}

	private static <T extends RealType<T>> void oneRun(double[] slope, final long[] translation,
		final RandomAccessibleInterval<T> image, Operation operation)
	{
		double combinedSlope = calculateCombinedSlope(slope, translation);
		RandomAccessibleInterval<T> centers = cutBorders(translation, image);
		RandomAccessibleInterval<T> neighbors = cutBorders(negate(translation), image);
		LoopBuilder.setImages(invertDirections(centers, translation), invertDirections(neighbors,
			translation))
			.flatIterationOrder()
			.forEachPixel(getAction(operation, combinedSlope));
	}

	private static <T extends RealType<T>> IntervalView<T> cutBorders(long[] translation,
		RandomAccessibleInterval<T> image)
	{
		return Views.interval(image, Intervals.intersect(image, Intervals.translate(image,
			translation)));
	}

	private static long[] negate(long[] values) {
		return LongStream.of(values).map(x -> -x).toArray();
	}

	private static <T extends RealType<T>> BiConsumer<T, T> getAction(Operation operation,
		double combinedSlope)
	{
		switch (operation) {
			case DILATION:
				return (center, neighbor) -> center.setReal(Math.max(center.getRealDouble(), neighbor
					.getRealDouble() - combinedSlope));
			case EROSION:
				return (center, neighbor) -> center.setReal(Math.min(center.getRealDouble(), neighbor
					.getRealDouble() + combinedSlope));
		}
		throw new AssertionError();
	}

	private static <T> RandomAccessibleInterval<T> invertDirections(RandomAccessibleInterval<T> pair,
		long[] translation)
	{
		for (int i = pair.numDimensions() - 1; i >= 0; i--)
			if (translation[i] < 0)
				return Views.invertAxis(pair, i);
		return pair;
	}

	private static double calculateCombinedSlope(double[] slope, long[] translation) {
		double combinedSlope = 0;
		for (int i = 0; i < slope.length; i++)
			combinedSlope += Math.pow(translation[i] * slope[i], 2);
		combinedSlope = Math.sqrt(combinedSlope);
		return combinedSlope;
	}

	enum Operation {
			DILATION, EROSION
	}
}
