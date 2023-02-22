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

import net.haesleinhuepf.clij.CLIJ;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A pool of {@link DefaultGpuApi}.
 * <p>
 * Which OpenCL devices are used and how threads should access it in parallel
 * can be configure by an environment variable "LABKIT_OPENCL_DEVICES".
 * <p>
 * Possible configurations are for example:
 * <ul>
 *     <li>"10 NVIDIA", 10 threads can access the NVIDIA CPU in parallel.</li>
 *     <li>"2 Intel; 4 NVIDIA", 2 threads should use the Intel GPU, 4 threads the NVIDIA GPU.</li>
 *     <li>"12 *", 12 threads may access whatever GPU is available.</li>
 *     <li>"12 device_id:0; 12 device_id:1", 12 threads should use the 1st GPU, 12 threads should use the 2nd GPU.</li>
 * </ul>
 */
public class GpuPool {

	private final BlockingDeque<Integer> deviceIds;

	private final GenericObjectPool<DefaultGpuApi> pool;

	private static final GpuPool POOL = new GpuPool();

	/**
	 * Borrow a {@link GpuApi} from the pool. It must be return after use by simply
	 * calling the {@link GpuApi#close()} method.
	 */
	public static GpuApi borrowGpu() {
		if (!isGpuAvailable())
			throw new IllegalStateException("No OpenCL device is available.");
		return POOL.gpu();
	}

	private GpuPool() {
		GenericObjectPoolConfig<DefaultGpuApi> config = new GenericObjectPoolConfig<>();
		deviceIds = new LinkedBlockingDeque<>(initializeDeviceIds());
		System.out.println("OpenCL device used: " + deviceIds);
		config.setMaxTotal(deviceIds.size());
		config.setMinIdle(0);
		config.setMinEvictableIdleTimeMillis(2000);
		config.setTimeBetweenEvictionRunsMillis(500);
		this.pool = new GenericObjectPool<>(new MyObjectFactory(), config);
		Runtime.getRuntime().addShutdownHook(new Thread(pool::close));
	}

	/**
	 * Borrow a {@link GpuApi} from the pool. It must be return after use by simply
	 * calling the {@link GpuApi#close()} method.
	 */
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

	/**
	 * Returns true if the {@link GpuPool} can give access to available GPUs.
	 */
	public static boolean isGpuAvailable() {
		return size() > 0;
	}

	/**
	 * The number of {@link GpuApi} objects that can be borrowed from the pool.
	 */
	public static int size() {
		return POOL.pool.getMaxTotal();
	}

	private class MyObjectFactory implements PooledObjectFactory<DefaultGpuApi> {

		@Override
		public PooledObject<DefaultGpuApi> makeObject() throws Exception {
			int deviceId = deviceIds.take();
			System.out.println("Create gpu context for device: " + deviceId);
			return new DefaultPooledObject<>(new DefaultGpuApi(deviceId));
		}

		@Override
		public void destroyObject(PooledObject<DefaultGpuApi> pooledObject) throws Exception {
			DefaultGpuApi gpuApi = pooledObject.getObject();
			gpuApi.close();
			int deviceId = gpuApi.getOpenClDeviceId();
			System.out.println("Return context for device: " + deviceId);
			deviceIds.put(deviceId);
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

	// ===============================================================================

	/**
	 * Returns a list of indices. The indices refer to the OpenCl device at the
	 * respective location in the list returned by {@code CLIJ.getAvailableDeviceNames()}.
	 * Each index should appear in the list several times. The number of times
	 * an index appears in the returned list, should define the number of {@link GpuApi}
	 * instances this GpuPool creates and distributes at the same time for this devices.
	 */
	private static List<Integer> initializeDeviceIds() {
		String configString = System.getenv("LABKIT_OPENCL_DEVICES");
		List<String> availableDeviceNames = getAvailableDeviceNames();
		if (availableDeviceNames == null || availableDeviceNames.isEmpty())
			return Collections.emptyList();
		if (configString == null || configString.isEmpty())
			return defaultOpenClDeviceIds(availableDeviceNames);
		return parseOpenClConfigString(configString, availableDeviceNames);

	}

	private static List<String> getAvailableDeviceNames() {
		try {
			return CLIJ.getAvailableDeviceNames();
		}
		catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	static List<Integer> parseOpenClConfigString(String configString, List<String> availableDeviceName) {
		List<Integer> result = new ArrayList<>();
		for(String config : configString.split(";")) {
			config = config.trim();
			int space = config.indexOf(" ");
			String first = config.substring(0, space);
			String second = config.substring(space + 1).trim();
			result.addAll(Collections.nCopies(Integer.parseInt(first), findOpenClDeviceId(second, availableDeviceName)));
		}
		return result;
	}

	static List<Integer> defaultOpenClDeviceIds(List<String> availableDeviceNames) {
		int deviceIndex = defaultOpenCLDeviceId(availableDeviceNames);
		CLIJ clij = new CLIJ(deviceIndex);
		try {
			long memory = clij.getGPUMemoryInBytes();
			int instances = (int) (memory / 500_000_000);
			return Collections.nCopies(instances, deviceIndex);
		}
		finally {
			clij.close();
		}
	}

	static int findOpenClDeviceId(String id, List<String> availableDeviceNames) {
		// parse ids like "device_id:2"
		if(id.startsWith("device_id:"))
			return Integer.parseInt(id.substring("device_id:".length()));
		// return the first id that isn't a cpu device
		if(id.equals("*"))
			return defaultOpenCLDeviceId(availableDeviceNames);
		// return first device that contains the string
		for(int i = 0; i < availableDeviceNames.size(); i++)
			if(availableDeviceNames.get(i).contains(id))
				return i;
		throw new RuntimeException("No such OpenCL device found: " + id);
	}

	private static int defaultOpenCLDeviceId(List<String> availableDeviceNames) {
		for (int i = 0; i < availableDeviceNames.size(); i++)
			if(!availableDeviceNames.get(i).contains("CPU"))
				return i;
		return 0;
	}
}
