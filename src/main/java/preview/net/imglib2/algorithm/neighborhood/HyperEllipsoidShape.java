/*
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

package preview.net.imglib2.algorithm.neighborhood;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.util.ConstantUtils;

/**
 * A factory for Accessibles on hyper-sphere neighboorhoods.
 *
 * @author Tobias Pietzsch
 * @author Jonathan Hale (University of Konstanz)
 */
public class HyperEllipsoidShape implements Shape {

	private final double[] radius;

	public HyperEllipsoidShape(final double[] radius) {
		this.radius = radius;
	}

	@Override
	public <T> NeighborhoodIterableInterval<T> neighborhoods(
		final RandomAccessibleInterval<T> source)
	{
		return new NeighborhoodIterableInterval<T>(source, HyperEllipsoidNeighborhoodUnsafe.factory(
			radius));
	}

	@Override
	public <T> NeighborhoodRandomAccessible<T> neighborhoodsRandomAccessible(
		final RandomAccessible<T> source)
	{
		return new NeighborhoodRandomAccessible<T>(source, HyperEllipsoidNeighborhoodUnsafe.factory(
			radius));
	}

	@Override
	public <T> NeighborhoodIterableInterval<T> neighborhoodsSafe(
		final RandomAccessibleInterval<T> source)
	{
		return new NeighborhoodIterableInterval<T>(source, HyperEllipsoidNeighborhood.factory(radius));
	}

	@Override
	public <T> NeighborhoodRandomAccessible<T> neighborhoodsRandomAccessibleSafe(
		final RandomAccessible<T> source)
	{
		return new NeighborhoodRandomAccessible<T>(source, HyperEllipsoidNeighborhood.factory(radius));
	}

	/**
	 * @return The radius of this shape.
	 */
	public double[] getRadius() {
		return radius.clone();
	}

	@Override
	public String toString() {
		return "HyperEllipsoidShape, radius = " + radius;
	}

	@Override
	public Interval getStructuringElementBoundingBox(final int numDimensions) {
		RandomAccess<Void> dummyRandomAccess = ConstantUtils.constantRandomAccessible((Void) null,
			numDimensions).randomAccess();
		Neighborhood<Void> neighborhood = HyperEllipsoidNeighborhood.factory(radius).create(
			new long[numDimensions], dummyRandomAccess);
		return neighborhood.getStructuringElementBoundingBox();
	}
}
