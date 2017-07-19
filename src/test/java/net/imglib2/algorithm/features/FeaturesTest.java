package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.GaussFeatureOp;
import net.imglib2.algorithm.features.ops.SphereShapedFeature;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Matthias Arzt
 */
public class FeaturesTest {
	@Test
	public void testCreate() {
		GaussFeatureOp op = Features.create(GaussFeatureOp.class, 1.3);
		assertNotNull(op);
	}
}
