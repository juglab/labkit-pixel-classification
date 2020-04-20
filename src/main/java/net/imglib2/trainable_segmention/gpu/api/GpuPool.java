
package net.imglib2.trainable_segmention.gpu.api;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class GpuPool {

	private static final String OPEN_CL_DEVICE_NAME = null;

	private static final int NUMBER_OF_OPENCL_CONTEXT_USED = 4;

	private final GenericObjectPool<DefaultGpuApi> pool;

	private static final GpuPool POOL = new GpuPool();

	public static GpuApi borrowGpu() {
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
