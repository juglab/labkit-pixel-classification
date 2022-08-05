/*
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;

public class NeighborhoodRandomAccess<T> extends NeighborhoodLocalizableSampler<T> implements
	RandomAccess<Neighborhood<T>>
{

	public NeighborhoodRandomAccess(final RandomAccessible<T> source,
		final NeighborhoodFactory factory)
	{
		super(source, factory, null);
	}

	public NeighborhoodRandomAccess(final RandomAccessible<T> source,
		final NeighborhoodFactory factory, final Interval interval)
	{
		super(source, factory, interval);
	}

	private NeighborhoodRandomAccess(final NeighborhoodRandomAccess<T> c) {
		super(c);
	}

	@Override
	public void fwd(final int d) {
		++currentPos[d];
	}

	@Override
	public void bck(final int d) {
		--currentPos[d];
	}

	@Override
	public void move(final int distance, final int d) {
		currentPos[d] += distance;
	}

	@Override
	public void move(final long distance, final int d) {
		currentPos[d] += distance;
	}

	@Override
	public void move(final Localizable localizable) {
		for (int d = 0; d < n; ++d)
			currentPos[d] += localizable.getLongPosition(d);
	}

	@Override
	public void move(final int[] distance) {
		for (int d = 0; d < n; ++d)
			currentPos[d] += distance[d];
	}

	@Override
	public void move(final long[] distance) {
		for (int d = 0; d < n; ++d)
			currentPos[d] += distance[d];
	}

	@Override
	public void setPosition(final Localizable localizable) {
		for (int d = 0; d < n; ++d)
			currentPos[d] = localizable.getLongPosition(d);
	}

	@Override
	public void setPosition(final int[] position) {
		for (int d = 0; d < n; ++d)
			currentPos[d] = position[d];
	}

	@Override
	public void setPosition(final long[] position) {
		for (int d = 0; d < n; ++d)
			currentPos[d] = position[d];
	}

	@Override
	public void setPosition(final int position, final int d) {
		currentPos[d] = position;
	}

	@Override
	public void setPosition(final long position, final int d) {
		currentPos[d] = position;
	}

	@Override
	public NeighborhoodRandomAccess<T> copy() {
		return new NeighborhoodRandomAccess<T>(this);
	}

	@Override
	public NeighborhoodRandomAccess<T> copyRandomAccess() {
		return copy();
	}
}
