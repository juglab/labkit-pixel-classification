package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.AbstractFeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.scijava.module.Module;
import org.scijava.plugin.Parameter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingsTest {
	@Test
	public void testCreation() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
	}

	@Test
	public void testListParameters() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		List<String> parameters = fs.parameters();
		assertEquals(Collections.singletonList("sigma"), parameters);
	}

	@Test
	public void testChangeParameterValues() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		double value1 = 1.0;
		fs.setParameter("sigma", value1);
		double result1 = (Double) fs.getParameter("sigma");
		double value2 = 4.2;
		fs.setParameter("sigma", value2);
		double result2 = (Double) fs.getParameter("sigma");
		assertEquals(value1, result1, 0.001);
		assertEquals(value2, result2, 0.001);
	}

	@Test
	public void getModule() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		Module module = fs.asModule();
		assertTrue(module.getDelegateObject() instanceof TestFeature);
	}

	@Test
	public void createInstance() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		double sigma = 4.2;
		fs.setParameter("sigma", sigma);
		TestFeature f = (TestFeature) fs.newInstance(RevampUtils.ops());
		assertTrue(f.isInitialized());
		assertEquals(sigma, f.sigma(), 0.001);
	}

	@Test
	public void testGetParameterType() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		Type type = fs.getParameterType("sigma");
		assertEquals(double.class, type);
	}

	public static class TestFeature extends AbstractFeatureOp {

		@Parameter
		private double sigma = 2.0;

		private boolean initialized = false;

		@Override
		public void initialize() {
			initialized = true;
		}

		public double sigma() {
			return sigma;
		}

		public boolean isInitialized() {
			assertNotNull(ops());
			return initialized;
		}

		@Override
		public int count() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<String> attributeLabels() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
			throw new UnsupportedOperationException();
		}
	}
}
