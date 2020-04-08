
package net.imglib2.trainable_segmention.gpu.api;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.HashMap;

public class DefaultGpuApi implements GpuApi {

	private final CLIJ2 clij;

	private final ClearCLBufferPool pool;

	public DefaultGpuApi(CLIJ2 clij) {
		this.clij = clij;
		this.pool = new ClearCLBufferPool(clij.getCLIJ().getClearCLContext());
	}

	@Override
	public GpuImage create(long[] dimensions, long numberOfChannels, NativeTypeEnum type) {
		return new GpuImage(pool.create(dimensions, numberOfChannels, type), pool::release);
	}

	@Override
	public GpuApi subScope() {
		return new GpuScope(this, false);
	}

	@Override
	public void close() {
		pool.close();
	}

	@Override
	public void execute(Class<?> anchorClass, String kernelFile, String kernelName,
		long[] globalSizes, long[] localSizes, HashMap<String, Object> parameters,
		HashMap<String, Object> defines)
	{
		for (String key : parameters.keySet()) {
			Object value = parameters.get(key);
			if (value instanceof GpuImage)
				parameters.put(key, ((GpuImage) value).clearCLBuffer());
		}
		clij.executeSubsequently(anchorClass, kernelFile, kernelName, null, globalSizes, localSizes,
			parameters, defines, null).close();
	}
}
