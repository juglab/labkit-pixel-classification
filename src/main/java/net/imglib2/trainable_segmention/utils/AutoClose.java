
package net.imglib2.trainable_segmention.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AutoClose} holds a list of {@link AutoCloseable}s. All autoclosables
 * are closed when {@link AutoClose#close} is called.
 */
public class AutoClose implements AutoCloseable {

	private final List<AutoCloseable> list = new ArrayList<>();

	public <T extends AutoCloseable> T add(T value) {
		list.add(value);
		return value;
	}

	@Override
	public void close() {
		Exception exception = null;
		for (int i = list.size() - 1; i >= 0; i--) {
			try {
				list.get(i).close();
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
