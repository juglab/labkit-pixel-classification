/*-
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

package sc.fiji.labkit.pixel_classification.gpu.api;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLContext;
import net.haesleinhuepf.clij.clearcl.enums.HostAccessType;
import net.haesleinhuepf.clij.clearcl.enums.KernelAccessType;
import net.haesleinhuepf.clij.clearcl.enums.MemAllocMode;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link ClearCLBufferPool} allows to easily reuse {@link ClearCLBuffer}s.
 * <p>
 * The method {@link #create} might be used as usual to get a
 * {@link ClearCLBuffer}, but the buffer might has been used before. After use a
 * buffer should be returned by calling {@link #release(ClearCLBuffer)}.
 */
class ClearCLBufferPool implements AutoCloseable {

	private final ClearCLContext context;

	private final Map<Specification, Queue<ClearCLBuffer>> unused = new ConcurrentHashMap<>();

	ClearCLBufferPool(ClearCLContext context) {
		this.context = context;
	}

	public ClearCLBuffer create(long[] dimensions, long numberOfChannels, NativeTypeEnum type) {
		Specification key = new Specification(dimensions, numberOfChannels, type);
		Queue<ClearCLBuffer> list = unused.get(key);
		if (list != null) {
			ClearCLBuffer buffer = list.poll();
			if (buffer != null)
				return buffer;
		}
		return context.createBuffer(MemAllocMode.Best, HostAccessType.ReadWrite,
			KernelAccessType.ReadWrite,
			numberOfChannels, type, dimensions);
	}

	public void release(ClearCLBuffer buffer) {
		Specification key = new Specification(buffer.getDimensions(), buffer.getNumberOfChannels(),
			buffer.getNativeType());
		Queue<ClearCLBuffer> list = unused.computeIfAbsent(key,
			ignore -> new ConcurrentLinkedQueue<>());
		list.add(buffer);
	}

	@Override
	public void close() {
		clear();
	}

	private void closeAll(Queue<ClearCLBuffer> list) {
		while (true) {
			ClearCLBuffer buffer = list.poll();
			if (buffer == null)
				break;
			buffer.close();
		}
	}

	public void clear() {
		for (Queue<ClearCLBuffer> list : unused.values())
			closeAll(list);
	}

	private static class Specification {

		private final long[] dimensions;

		private final long numberOfChannels;

		private final NativeTypeEnum type;

		private final int hash;

		private Specification(long[] dimensions, long numberOfChannels, NativeTypeEnum type) {
			this.dimensions = dimensions.clone();
			this.numberOfChannels = numberOfChannels;
			this.type = type;
			this.hash = Objects.hash(Arrays.hashCode(dimensions), numberOfChannels, type);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Specification &&
				Arrays.equals(dimensions, ((Specification) obj).dimensions) &&
				numberOfChannels == ((Specification) obj).numberOfChannels &&
				type.equals(((Specification) obj).type);
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}
}
