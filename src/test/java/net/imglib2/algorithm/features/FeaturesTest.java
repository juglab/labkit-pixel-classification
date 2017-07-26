package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.SingleGaussFeature;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Matthias Arzt
 */
public class FeaturesTest {
	@Test
	public void testCreate() {
		SingleGaussFeature op = Features.create(SingleGaussFeature.class, GlobalSettings.defaultSettings(), 1.3);
		assertNotNull(op);
	}
}
