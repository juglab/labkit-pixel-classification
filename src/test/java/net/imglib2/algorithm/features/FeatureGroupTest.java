package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.algorithm.features.ops.SingleGaussFeature;
import org.junit.Test;

/**
 * @author Matthias Arzt
 */
public class FeatureGroupTest {

	@Test
	public void testEqualFeatureSettings() {
		GlobalSettings settingsA = new GlobalSettings(2.0, 16.0, 1.0);
		FeatureOp a = Features.create(SingleGaussFeature.class, settingsA,1.0)	;
		GlobalSettings settingsB = new GlobalSettings(2.0, 16.0, 1.0);
		FeatureOp b = Features.create(SingleGaussFeature.class, settingsB,1.0)	;
		Features.group(a, b);
	}
}
