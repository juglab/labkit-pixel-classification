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

package sc.fiji.labkit.pixel_classification.gpu.api;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.util.function.Consumer;

public class GpuImage implements AutoCloseable {

	private ClearCLBuffer clearClBuffer;

	private final Consumer<ClearCLBuffer> onClose;

	public GpuImage(ClearCLBuffer clearClBuffer, Consumer<ClearCLBuffer> onClose) {
		this.clearClBuffer = clearClBuffer;
		this.onClose = onClose;
	}

	public long[] getDimensions() {
		return clearClBuffer.getDimensions();
	}

	public long getWidth() {
		return clearClBuffer.getWidth();
	}

	public long getHeight() {
		return clearClBuffer.getHeight();
	}

	public long getDepth() {
		return clearClBuffer.getDepth();
	}

	public ClearCLBuffer clearCLBuffer() {
		return clearClBuffer;
	}

	@Override
	public void close() {
		if (clearClBuffer == null)
			return;
		ClearCLBuffer buffer = clearClBuffer;
		clearClBuffer = null;
		onClose.accept(buffer);
	}

	public NativeTypeEnum getNativeType() {
		return clearClBuffer.getNativeType();
	}

	public long getNumberOfChannels() {
		return clearClBuffer.getNumberOfChannels();
	}
}
