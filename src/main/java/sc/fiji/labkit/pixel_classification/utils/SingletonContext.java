
package net.imglib2.trainable_segmentation.utils;

import org.scijava.Context;

/**
 * Singleton that provides a scijava {@link Context}.
 * <p>
 * It provides thread-safe, lazy initialization.
 */
public class SingletonContext {

	private SingletonContext() {
		// prevent from instantiation
	}

	private static Context context;

	public static Context getInstance() {
		if (context == null)
			initialize();
		return context;
	}

	private static synchronized void initialize() {
		if (context == null)
			context = new Context();
	}
}
