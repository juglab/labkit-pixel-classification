/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.pixel_feature.settings;

import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.gson.GsonUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.identity.IdentityFeature;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.scijava.module.Module;
import org.scijava.plugin.Parameter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingTest {

	@Test
	public void testCreation() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
	}

	@Test
	public void testListParameters() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		Set<String> parameters = fs.parameters();
		assertEquals(Collections.singleton("sigma"), parameters);
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
		Module module = fs.asModule(GlobalSettings.default2d().build());
		assertTrue(module.getDelegateObject() instanceof TestFeature);
	}

	@Test
	public void createInstance() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		double sigma = 4.2;
		fs.setParameter("sigma", sigma);
		TestFeature f = (TestFeature) fs.newInstance(SingletonContext.getInstance(), GlobalSettings
			.default2d().build());
		assertTrue(f.isInitialized());
		assertEquals(sigma, f.sigma(), 0.001);
	}

	@Test
	public void testGetParameterType() {
		FeatureSetting fs = FeatureSetting.fromClass(TestFeature.class);
		Type type = fs.getParameterType("sigma");
		assertEquals(double.class, type);
	}

	@Test
	public void testEquals() {
		FeatureSetting fs1 = FeatureSetting.fromClass(TestFeature.class);
		FeatureSetting fs2 = FeatureSetting.fromClass(TestFeature.class);
		boolean equal = fs1.equals(fs2);
		boolean equalHashes = fs1.hashCode() == fs2.hashCode();
		assertTrue(equal);
		assertTrue(equalHashes);
	}

	@Test
	public void testToJson() {
		FeatureSetting fs = new FeatureSetting(TestFeature.class, "sigma", 7.0);
		String json = GsonUtils.toString(fs.toJsonTree());
		String expected = "{\"class\":\"" + TestFeature.class.getName() + "\",\"sigma\":7.0}";
		assertEquals(expected, json);
	}

	@Test
	public void testFromJson() {
		String json = "{\"class\":\"" + TestFeature.class.getName() + "\",\"sigma\":7.0}";
		FeatureSetting fs = FeatureSetting.fromJson(GsonUtils.fromString(json));
		FeatureSetting expected = new FeatureSetting(TestFeature.class, "sigma", 7.0);
		assertEquals(expected, fs);
	}

	@Test
	public void testBackwardCompatibility() {
		// NB: Packages have been renamed in this code base twice:
		//   net.imglib2.trainable_segmention -> net.imglib2.trainable_segmentation
		//   net.imglib2.trainable_segmentation -> sc.fiji.labkit.pixel_classification
		// This test assures that class names stored in old classifiers
		// are translated correctly to the current package name / class names.
		testBackwardCompatibility( "net.imglib2.trainable_segmentation.pixel_feature.filter.identity.IdentityFeature", IdentityFeature.class );

		// The oldest version of this code had a typo in it's package name.
		// The package name was fix simultaneously with replacing old filters
		// by better implementations. The class names with typos there refer
		// to now deprecated filters.
		testBackwardCompatibility( "net.imglib2.trainable_segmention.pixel_feature.filter.identity.IdendityFeature",
				sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.identity.IdendityFeature.class );
	}

	private void testBackwardCompatibility( String oldClassName, Class<?> newClass )
	{
		String json = "{\"class\":\"" + oldClassName + "\"}";
		FeatureSetting fs = FeatureSetting.fromJson(GsonUtils.fromString(json));
		assertEquals( newClass, fs.pluginClass());
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
		public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
			throw new UnsupportedOperationException();
		}
	}
}
