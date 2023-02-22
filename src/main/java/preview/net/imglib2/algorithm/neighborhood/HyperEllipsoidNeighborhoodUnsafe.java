/*
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

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;

public final class HyperEllipsoidNeighborhoodUnsafe<T> extends HyperEllipsoidNeighborhood<T> {

	public static NeighborhoodFactory factory(final double[] radius) {
		return new NeighborhoodFactory() {

			@Override
			public <T> Neighborhood<T> create(long[] position, RandomAccess<T> sourceRandomAccess) {
				return new HyperEllipsoidNeighborhoodUnsafe<>(position, radius, sourceRandomAccess);
			}
		};
	}

	private final LocalCursor theCursor;

	private final LocalCursor firstElementCursor;

	HyperEllipsoidNeighborhoodUnsafe(final long[] position, final double[] radius,
		final RandomAccess<T> sourceRandomAccess)
	{
		super(position, radius, sourceRandomAccess);
		theCursor = super.cursor();
		firstElementCursor = super.cursor();
	}

	@Override
	public T firstElement() {
		firstElementCursor.reset();
		return firstElementCursor.next();
	}

	@Override
	public LocalCursor cursor() {
		theCursor.reset();
		return theCursor;
	}
}
