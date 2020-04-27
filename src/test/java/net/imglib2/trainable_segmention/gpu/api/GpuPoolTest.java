
package net.imglib2.trainable_segmention.gpu.api;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Test {@link GpuPool}.
 */
public class GpuPoolTest {

	@Test
	public void testGpuAptReuse() {
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
}
