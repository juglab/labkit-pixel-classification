
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * All {@link ClearCLBuffer} created with this scope are automatically given
 * back if the scope is closed.
 */
public class ReuseScope implements AutoCloseable {

	private final ClearCLBufferReuse reuse;
	private final Queue<ClearCLBuffer> queue = new ConcurrentLinkedQueue<>();

	public ReuseScope(ClearCLBufferReuse reuse) {
		this.reuse = reuse;
	}

	public ClearCLBuffer create(long... dimensions) {
		return add(reuse.create(dimensions));
	}

	ClearCLBuffer add(ClearCLBuffer buffer) {
		queue.add(buffer);
		return buffer;
	}

	@Override
	public void close() {
		while (true) {
			ClearCLBuffer buffer = queue.poll();
			if (buffer == null)
				break;
			reuse.giveBack(buffer);
		}
	}
}
