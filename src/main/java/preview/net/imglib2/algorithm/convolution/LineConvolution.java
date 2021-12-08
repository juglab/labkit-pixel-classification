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

package preview.net.imglib2.algorithm.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import preview.net.imglib2.loops.LoopBuilder;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.view.Views;

/**
 * This class can be used to implement a separable convolution. It applies a
 * {@link preview.net.imglib2.algorithm.convolution.LineConvolverFactory} on the
 * given images.
 *
 * @author Matthias Arzt
 */
public class LineConvolution<T> implements Convolution<T> {

	private final preview.net.imglib2.algorithm.convolution.LineConvolverFactory<? super T> factory;

	private final int direction;

	public LineConvolution(
		final preview.net.imglib2.algorithm.convolution.LineConvolverFactory<? super T> factory,
		final int direction)
	{
		this.factory = factory;
		this.direction = direction;
	}

	@Override
	public Interval requiredSourceInterval(final Interval targetInterval) {
		final long[] min = Intervals.minAsLongArray(targetInterval);
		final long[] max = Intervals.maxAsLongArray(targetInterval);
		min[direction] -= factory.getBorderBefore();
		max[direction] += factory.getBorderAfter();
		return new FinalInterval(min, max);
	}

	@Override
	public T preferredSourceType(final T targetType) {
		return (T) factory.preferredSourceType(targetType);
	}

	@Override
	public void process(RandomAccessible<? extends T> source,
		RandomAccessibleInterval<? extends T> target)
	{
		final RandomAccessibleInterval<? extends T> sourceInterval = Views.interval(source,
			requiredSourceInterval(target));
		final long[] sourceMin = Intervals.minAsLongArray(sourceInterval);
		final long[] targetMin = Intervals.minAsLongArray(target);

		final long[] dim = Intervals.dimensionsAsLongArray(target);
		dim[direction] = 1;

		RandomAccessibleInterval<Localizable> positions = Views.interval(Localizables.randomAccessible(
			dim.length), new FinalInterval(dim));
		LoopBuilder.setImages(positions).multiThreaded().forEachChunk(
			chunk -> {

				final RandomAccess<? extends T> in = sourceInterval.randomAccess();
				final RandomAccess<? extends T> out = target.randomAccess();
				final Runnable convolver = factory.getConvolver(in, out, direction, target.dimension(
					direction));

				chunk.forEachPixel(position -> {
					in.setPosition(sourceMin);
					out.setPosition(targetMin);
					in.move(position);
					out.move(position);
					convolver.run();
				});

				return null;
			});
	}
}
