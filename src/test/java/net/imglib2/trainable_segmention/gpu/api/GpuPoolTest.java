
package net.imglib2.trainable_segmention.gpu.api;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class GpuPoolTest {

	@Test
	public void testSameBuffer() {
		ClearCLBuffer a;
		ClearCLBuffer b;
		try (GpuApi gpu = GpuPool.borrowGpu()) {
			a = gpu.create(new long[] { 10, 10 }, NativeTypeEnum.Float).clearCLBuffer();
		}
		try (GpuApi gpu = GpuPool.borrowGpu()) {
			b = gpu.create(new long[] { 10, 10 }, NativeTypeEnum.Float).clearCLBuffer();
		}
		assertSame(a, b);
	}

	@Test
	public void testDifferentBuffer() throws InterruptedException {
		ClearCLBuffer a;
		ClearCLBuffer b;
		try (GpuApi gpu = GpuPool.borrowGpu()) {
			a = gpu.create(new long[] { 10, 10 }, NativeTypeEnum.Float).clearCLBuffer();
		}

		// Not using the GPU for 10 seconds should close all buffers
		Thread.sleep(10000);

		try (GpuApi gpu = GpuPool.borrowGpu()) {
			b = gpu.create(new long[] { 10, 10 }, NativeTypeEnum.Float).clearCLBuffer();
		}
		assertNotSame(a, b);
	}
}
