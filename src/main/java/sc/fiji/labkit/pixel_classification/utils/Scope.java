
package net.imglib2.trainable_segmentation.utils;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * {@link Scope} holds a list of {@link AutoCloseable}s. All autoclosables are
 * closed when {@link Scope#close} is called.
 */
public class Scope implements AutoCloseable {

	private final Deque<AutoCloseable> closeQueue = new ConcurrentLinkedDeque<>();

	public <T extends AutoCloseable> T register(T value) {
		closeQueue.addLast(value);
		return value;
	}

	@Override
	public void close() {
		Exception exception = null;
		while (true) {
			try {
				AutoCloseable item = closeQueue.pollLast();
				if (item == null)
					break;
				item.close();
			}
			catch (Exception e) {
				if (exception == null)
					exception = e;
			}
		}
		if (exception != null) {
			if (exception instanceof RuntimeException)
				throw (RuntimeException) exception;
			else
				throw new RuntimeException(exception);
		}
	}
}
