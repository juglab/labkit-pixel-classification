
package net.imglib2.trainable_segmentation.gpu.api;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assume.assumeTrue;

public class AbstractGpuTest {

	protected GpuApi gpu;

	@Before
	public void before() {
		assumeTrue("OpenCL device is not available.", GpuPool.isGpuAvailable());
		gpu = GpuPool.borrowGpu();
	}

	@After
	public void after() {
		if (gpu != null)
			gpu.close();
	}
}
