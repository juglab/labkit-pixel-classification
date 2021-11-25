
package sc.fiji.labkit.pixel_classification.gpu.api;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.backend.jocl.ClearCLBackendJOCL;
import net.haesleinhuepf.clij.clearcl.exceptions.OpenCLException;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

public class DefaultGpuApi implements GpuApi {

	private final int deviceId;

	private final CLIJ2 clij;

	private final ClearCLBufferPool pool;

	private final static Set<ClearCLBufferPool> POOLS = new CopyOnWriteArraySet<>();

	DefaultGpuApi(int deviceId) {
		this.deviceId = deviceId;
		this.clij = createCLIJ2(deviceId);
		this.pool = new ClearCLBufferPool(clij.getCLIJ().getClearCLContext());
		POOLS.add(pool);
	}

	public int getOpenClDeviceId() {
		return deviceId;
	}

	public static synchronized CLIJ2 createCLIJ2(int deviceId) {
		return new CLIJ2(new CLIJ(deviceId));
	}

	public static boolean isDeviceAvailable() {
		try {
			return CLIJ.getAvailableDeviceNames().isEmpty();
		}
		catch (Throwable e) {
			return false;
		}
	}

	@Override
	public GpuImage create(long[] dimensions, long numberOfChannels, NativeTypeEnum type) {
		return handleOutOfMemoryException(() -> new GpuImage(pool.create(dimensions, numberOfChannels,
			type), pool::release));
	}

	@Override
	public GpuApi subScope() {
		return new GpuScope(this, null);
	}

	@Override
	public void close() {
		POOLS.remove(pool);
		try {
			pool.close();
		}
		catch (Exception ignored) {}
		try {
			clij.close();
		}
		catch (Exception ignored) {}
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
		handleOutOfMemoryException(() -> {
			clij.executeSubsequently(anchorClass, kernelFile, kernelName, null, globalSizes, localSizes,
				parameters, defines, null).close();
			return null;
		});
	}

	@Override
	public <T> T handleOutOfMemoryException(Supplier<T> action) {
		try {
			return action.get();
		}
		catch (OpenCLException exception) {
			if (exception.getErrorCode() == -4) {
				// TODO: Add log message for garbage collection.
				POOLS.forEach(ClearCLBufferPool::clear);
				return action.get();
			}
			else
				throw exception;
		}
	}

}
