
package net.imglib2.trainable_segmention.gpu.api;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.trainable_segmention.utils.Scope;

import java.util.HashMap;

public class GpuScope implements GpuApi {

	private final Scope closeQueue = new Scope();

	private final boolean closeParent;

	private final GpuApi parent;

	public GpuScope(GpuApi parent, boolean closeParent) {
		this.parent = parent;
		this.closeParent = closeParent;
	}

	@Override
	public GpuImage create(long[] dimensions, long numberOfChannels, NativeTypeEnum type) {
		return closeQueue.register(parent.create(dimensions, numberOfChannels, type));
	}

	@Override
	public GpuApi subScope() {
		return closeQueue.register(new GpuScope(parent, closeParent));
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
	public void close() {
		closeQueue.close();
	}
}
