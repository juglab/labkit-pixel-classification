package net.imglib2.trainable_segmention.utils;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests {@link AutoClose}.
 */
public class AutoCloseTest {

	@Test
	public void testCreate() {
		try (AutoClose list = new AutoClose()) {

		}
	}

	@Test
	public void testClose() {
		boolean[] closed = {false};
		AutoClose list = new AutoClose();
		list.add(() -> closed[0] = true);
		assertFalse(closed[0]);
		list.close();
		assertTrue(closed[0]);
	}

	@Test
	public void testException() {
		String message = null;
		try (AutoClose list = new AutoClose()) {
			list.add(() -> {});
			list.add(() -> { throw new RuntimeException("a"); });
			list.add(() -> { throw new RuntimeException("b"); });
			list.add(() -> {});
		} catch( RuntimeException e ) {
			message = e.getMessage();
		}
		assertEquals("b", message);
	}
}
