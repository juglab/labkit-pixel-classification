
package clij;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * All {@link GpuImage} created with this scope are automatically closed when
 * this scope is closed.
 */
public class GpuScope implements AutoCloseable {

	private final GpuApi gpu;

	private final Queue<GpuImage> queue = new ConcurrentLinkedQueue<>();

	public GpuScope(GpuApi gpu) {
		this.gpu = gpu;
	}

	public GpuImage create(long... dimensions) {
		return add(gpu.create(dimensions, NativeTypeEnum.Float));
	}

	GpuImage add(GpuImage buffer) {
		queue.add(buffer);
		return buffer;
	}

	@Override
	public void close() {
		while (true) {
			GpuImage buffer = queue.poll();
			if (buffer == null)
				break;
			buffer.close();
		}
	}
}
