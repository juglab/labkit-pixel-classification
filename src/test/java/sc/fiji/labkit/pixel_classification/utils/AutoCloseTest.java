
package sc.fiji.labkit.pixel_classification.utils;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests {@link Scope}.
 */
public class AutoCloseTest {

	@Test
	public void testCreate() {
		try (Scope list = new Scope()) {

		}
	}

	@Test
	public void testClose() {
		boolean[] closed = { false };
		Scope list = new Scope();
		list.register(() -> closed[0] = true);
		assertFalse(closed[0]);
		list.close();
		assertTrue(closed[0]);
	}

	@Test
	public void testException() {
		String message = null;
		try (Scope list = new Scope()) {
			list.register(() -> {});
			list.register(() -> {
				throw new RuntimeException("a");
			});
			list.register(() -> {
				throw new RuntimeException("b");
			});
			list.register(() -> {});
		}
		catch (RuntimeException e) {
			message = e.getMessage();
		}
		assertEquals("b", message);
	}
}
