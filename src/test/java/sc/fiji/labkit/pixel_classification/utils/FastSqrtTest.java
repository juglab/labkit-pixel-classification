
package sc.fiji.labkit.pixel_classification.utils;

import sc.fiji.labkit.pixel_classification.utils.FastSqrt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FastSqrtTest {

	@Test
	public void testSqrt() {
		float sqrt = FastSqrt.sqrt((float) 5);
		assertEquals(5, sqrt * sqrt, 0);
	}

	@Test
	public void testSqrtForDouble() {
		double sqrt = FastSqrt.sqrt((double) 5);
		assertEquals(5, sqrt * sqrt, 1.e-10);
	}

}
