
package net.imglib2.trainable_segmentation.gpu.api;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class GpuPool {

	private static final String OPEN_CL_DEVICE_NAME = null;

	private static final int NUMBER_OF_OPENCL_CONTEXT_USED = 4;

	private final GenericObjectPool<DefaultGpuApi> pool;

	private static final GpuPool POOL = initializePool();

	private static GpuPool initializePool() {
		return DefaultGpuApi.isDeviceAvailable(OPEN_CL_DEVICE_NAME) ? new GpuPool() : null;
	}

	public static GpuApi borrowGpu() {
		if (!isGpuAvailable())
			throw new IllegalStateException("No OpenCL device is available. " + OPEN_CL_DEVICE_NAME);
		return POOL.gpu();
	}

	private GpuPool() {
		GenericObjectPoolConfig<DefaultGpuApi> config = new GenericObjectPoolConfig<>();
		config.setMaxTotal(NUMBER_OF_OPENCL_CONTEXT_USED);
		config.setMinIdle(0);
		config.setMinEvictableIdleTimeMillis(2000);
		config.setTimeBetweenEvictionRunsMillis(500);
		this.pool = new GenericObjectPool<>(new MyObjectFactory(), config);
		Runtime.getRuntime().addShutdownHook(new Thread(pool::close));
	}

	public GpuApi gpu() {
		try {
			DefaultGpuApi gpu = pool.borrowObject();
			return new GpuScope(gpu, () -> {
				// this is executed when the GpuScope is closed
				pool.returnObject(gpu);
			});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isGpuAvailable() {
		return POOL != null;
	}

	private static class MyObjectFactory implements PooledObjectFactory<DefaultGpuApi> {

		@Override
		public PooledObject<DefaultGpuApi> makeObject() throws Exception {
			return new DefaultPooledObject<>(new DefaultGpuApi(OPEN_CL_DEVICE_NAME));
		}

		@Override
		public void destroyObject(PooledObject<DefaultGpuApi> pooledObject) {
			pooledObject.getObject().close();
		}

		@Override
		public boolean validateObject(PooledObject<DefaultGpuApi> pooledObject) {
			return true;
		}

		@Override
		public void activateObject(PooledObject<DefaultGpuApi> pooledObject) {

		}

		@Override
		public void passivateObject(PooledObject<DefaultGpuApi> pooledObject) {

		}
	}
}
