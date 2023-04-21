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

package sc.fiji.labkit.pixel_classification.utils.views;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.util.Cast;
import net.imglib2.view.composite.Composite;
import net.imglib2.loops.ClassCopyProvider;

public class CompositeRandomAccess<T> implements RandomAccess<Composite<T>>, Composite<T> {

	private static final ClassCopyProvider<RandomAccess> provide =
		new ClassCopyProvider<>(CompositeRandomAccess.class, RandomAccess.class);

	public static <T> RandomAccess<Composite<T>> create(RandomAccess<T> ra) {
		return Cast.unchecked(provide.newInstanceForKey(ra.getClass(), ra));
	}

	private final RandomAccess<T> ra;

	private final int n;

	public CompositeRandomAccess(RandomAccess<T> ra) {
		this.ra = ra;
		this.n = ra.numDimensions() - 1;
	}

	@Override
	public RandomAccess<Composite<T>> copy() {
		return new CompositeRandomAccess<>(ra.copy());
	}

	@Override
	public Composite<T> get() {
		return this;
	}

	@Override
	public T get(long i) {
		ra.setPosition(i, n);
		return ra.get();
	}

	@Override
	public long getLongPosition(int d) {
		return ra.getLongPosition(d);
	}

	@Override
	public void fwd(int d) {
		ra.fwd(d);
	}

	@Override
	public void bck(int d) {
		ra.bck(d);
	}

	@Override
	public void move(int distance, int d) {
		ra.move(distance, d);
	}

	@Override
	public void move(long distance, int d) {
		ra.move(distance, d);
	}

	@Override
	public void move(Localizable distance) {
		for (int d = 0; d < n; d++)
			move(distance.getLongPosition(d), d);
	}

	@Override
	public void move(int[] distance) {
		for (int d = 0; d < n; d++)
			move(distance[d], d);

	}

	@Override
	public void move(long[] distance) {
		for (int d = 0; d < n; d++)
			move(distance[d], d);
	}

	@Override
	public void setPosition(Localizable position) {
		for (int d = 0; d < n; d++)
			setPosition(position.getLongPosition(d), d);
	}

	@Override
	public void setPosition(int[] position) {
		for (int d = 0; d < n; d++)
			setPosition(position[d], d);
	}

	@Override
	public void setPosition(long[] position) {
		for (int d = 0; d < n; d++)
			setPosition(position[d], d);
	}

	@Override
	public void setPosition(int position, int d) {
		ra.setPosition(position, d);
	}

	@Override
	public void setPosition(long position, int d) {
		ra.setPosition(position, d);
	}

	@Override
	public int numDimensions() {
		return n;
	}
}
