package net.imglib2.trainable_segmention.utils;

import net.imglib2.trainable_segmention.utils.FastSqrt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FastSqrtTest {

	@Test
	public void testSqrt() {
		float sqrt = FastSqrt.sqrt((float) 5);
		assertEquals(5, sqrt *sqrt, 0);
	}

	@Test
	public void testSqrtForDouble() {
		double sqrt = FastSqrt.sqrt((double) 5);
		assertEquals(5, sqrt *sqrt, 0);
	}

}
