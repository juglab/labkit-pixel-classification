
package clij;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * All {@link GpuImage} created with this scope are automatically given back if
 * the scope is closed.
 */
public class ReuseScope implements AutoCloseable {

	private final ClearCLBufferReuse reuse;
	private final Queue<GpuImage> queue = new ConcurrentLinkedQueue<>();

	public ReuseScope(ClearCLBufferReuse reuse) {
		this.reuse = reuse;
	}

	public GpuImage create(long... dimensions) {
		return add(reuse.create(dimensions));
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
			reuse.giveBack(buffer);
		}
	}
}
