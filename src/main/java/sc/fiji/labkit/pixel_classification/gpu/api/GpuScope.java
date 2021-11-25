
package net.imglib2.trainable_segmentation.gpu.api;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.trainable_segmentation.utils.Scope;

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
