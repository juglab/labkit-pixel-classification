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

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import sc.fiji.labkit.pixel_classification.utils.Scope;

import java.util.HashMap;
import java.util.function.Supplier;

public class GpuScope implements GpuApi {

	private final Scope scope = new Scope();

	final GpuApi parent;

	public GpuScope(GpuApi parent, AutoCloseable onClose) {
		this.parent = parent;
		if (onClose != null)
			scope.register(onClose);
	}

	@Override
	public GpuImage create(long[] dimensions, long numberOfChannels, NativeTypeEnum type) {
		return scope.register(parent.create(dimensions, numberOfChannels, type));
	}

	@Override
	public GpuApi subScope() {
		return scope.register(new GpuScope(parent, null));
	}

	@Override
	public void execute(Class<?> anchorClass, String kernelFile, String kernelName,
		long[] globalSizes, long[] localSizes, HashMap<String, Object> parameters,
		HashMap<String, Object> defines)
	{
		parent.execute(anchorClass, kernelFile, kernelName, globalSizes, localSizes, parameters,
			defines);
	}

	@Override
	public <T> T handleOutOfMemoryException(Supplier<T> action) {
		return parent.handleOutOfMemoryException(action);
	}

	@Override
	public void close() {
		scope.close();
	}
}
