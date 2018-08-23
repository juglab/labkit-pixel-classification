package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import org.junit.Test;

import java.util.function.DoubleUnaryOperator;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class DerivedNormalDistributionTest {

	@Test
	public void testDerviceNormalDistribution() {
		DoubleUnaryOperator f = DerivedNormalDistribution.derivedNormalDistribution(5);
		assertEquals(0.9718373972, f.applyAsDouble(2.0), 0.00001);
		assertEquals(-1.4518243471, f.applyAsDouble(1.0), 0.00001);
		assertEquals(0.0, f.applyAsDouble(0.0), 0.00001);
	}
}
