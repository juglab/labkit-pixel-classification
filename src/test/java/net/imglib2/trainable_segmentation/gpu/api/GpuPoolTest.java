
package net.imglib2.trainable_segmentation.gpu.api;

import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLDevice;
import net.haesleinhuepf.clij.clearcl.backend.ClearCLBackends;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Test {@link GpuPool}.
 */
public class GpuPoolTest {

	@Test
	public void testGpuApiReuse() {
		assumeTrue(GpuPool.isGpuAvailable());
		GpuApi a;
		GpuApi b;
		try (GpuApi gpu = GpuPool.borrowGpu()) {
			a = ((GpuScope) gpu).parent;
		}
		try (GpuApi gpu = GpuPool.borrowGpu()) {
			b = ((GpuScope) gpu).parent;
		}
		assertSame(a, b);
	}

	@Test
	public void testGpuApiTimeout() throws InterruptedException {
		assumeTrue(GpuPool.isGpuAvailable());
		GpuApi a;
		GpuApi b;
		try (GpuApi gpu = GpuPool.borrowGpu()) {
			a = ((GpuScope) gpu).parent;
		}

		// Not using the GPU for 3 seconds should close all buffers
		Thread.sleep(3000);

		try (GpuApi gpu = GpuPool.borrowGpu()) {
			b = ((GpuScope) gpu).parent;
		}
		assertNotSame(a, b);
	}

	@Test
	public void testDefaultDeviceSettings() {
		ClearCL clearCL = new ClearCL(ClearCLBackends.getBestBackend());
		ClearCLDevice device = clearCL.getBestCPUDevice();
		System.out.println(device.getName());
		System.out.println(device.getGlobalMemorySizeInBytes() / 500_000_000);
		clearCL.close();
	}

	@Test
	public void test() {
		assertEquals(0, GpuPool.findOpenClDeviceId("*", Arrays.asList("CPU")));
		assertEquals(0, GpuPool.findOpenClDeviceId("*", Arrays.asList("GPU")));
		assertEquals(1, GpuPool.findOpenClDeviceId("B", Arrays.asList("someA", "someB")));
		assertEquals(1, GpuPool.findOpenClDeviceId("*", Arrays.asList("CPU backend", "GPU backend")));
		assertEquals(0, GpuPool.findOpenClDeviceId("*", Arrays.asList("GPU", "CPU")));
		assertEquals(5, GpuPool.findOpenClDeviceId("device_id:5", Arrays.asList("GPU")));
	}

	@Test
	public void testInitializeDeviceIds() {
		assertEquals(Arrays.asList(0,0), GpuPool.parseOpenClConfigString("2 GPU", Arrays.asList("GPU")));
		assertEquals(Arrays.asList(1,1,2,2,2,2), GpuPool.parseOpenClConfigString("2 Intel; 4 NVIDIA", Arrays.asList("CPU", "Intel", "NVIDIA")));
		assertEquals(Arrays.asList(0,0,0,1,1), GpuPool.parseOpenClConfigString("3 device_id:0; 2 device_id:1", Arrays.asList("CPU", "Intel", "NVIDIA")));
	}
}
