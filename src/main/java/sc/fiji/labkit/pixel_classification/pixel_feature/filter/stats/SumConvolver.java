/*-
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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats;

import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.algorithm.convolution.LineConvolverFactory;

import java.util.Arrays;

class SumConvolver implements Runnable {

	static <T extends RealType<?>> LineConvolverFactory<T> factory(long before, long after) {
		return new LineConvolverFactory<T>() {

			@Override
			public long getBorderBefore() {
				return before;
			}

			@Override
			public long getBorderAfter() {
				return after;
			}

			@Override
			public Runnable getConvolver(RandomAccess<? extends T> in, RandomAccess<? extends T> out,
				int d, long lineLength)
			{
				return new SumConvolver(before + 1 + after, in, out, d, lineLength);
			}

			@Override
			public T preferredSourceType(T targetType) {
				return targetType;
			}
		};
	}

	private final RandomAccess<? extends RealType<?>> in;

	private final RandomAccess<? extends RealType<?>> out;

	private final int d;

	private final int width;

	private final double[] buffer;

	private final long length;

	public SumConvolver(long width, RandomAccess<? extends RealType<?>> in,
		RandomAccess<? extends RealType<?>> out,
		int d, long length)
	{
		this.in = in;
		this.out = out;
		this.d = d;
		if (width >= (long) Integer.MAX_VALUE - 8)
			throw new IllegalArgumentException(
				"Width of SumConvolver needs to be less than Integer.MAX - 8");
		this.width = (int) width;
		this.buffer = new double[this.width];
		this.length = length;
	}

	@Override
	public void run() {
		Arrays.fill(buffer, 0);
		double sumA = 0;
		for (int i = 1; i < width; i++) {
			double value = in.get().getRealDouble();
			in.fwd(d);
			buffer[i] = value;
			sumA += value;
		}
		double remaining = this.length;
		while (remaining > width) {
			double sumB = 0;
			for (int i = 0; i < width; i++) {
				sumA -= buffer[i];
				double value = in.get().getRealDouble();
				in.fwd(d);
				buffer[i] = value;
				sumB += value;
				out.get().setReal(sumA + sumB);
				out.fwd(d);
			}
			remaining -= width;
			sumA = sumB;
		}
		for (int i = 0; i < remaining; i++) {
			sumA += in.get().getRealDouble() - buffer[i];
			in.fwd(d);
			out.get().setReal(sumA);
			out.fwd(d);
		}
	}
}
