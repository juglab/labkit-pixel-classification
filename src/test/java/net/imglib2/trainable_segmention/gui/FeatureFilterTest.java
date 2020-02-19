
package net.imglib2.trainable_segmention.gui;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import org.junit.Test;
import org.scijava.Context;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.fail;

public class FeatureFilterTest {

	private static final Context context = new Context();

	private static final Collection<Class<? extends FeatureOp>> only3d = Arrays.asList(
		net.imglib2.trainable_segmention.pixel_feature.filter.hessian.Hessian3DFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.hessian.SingleHessian3DFeature.class);

	private static final Collection<Class<? extends FeatureOp>> only2d = Arrays.asList(
		net.imglib2.trainable_segmention.pixel_feature.filter.gabor.SingleGaborFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.gabor.GaborFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.hessian.HessianFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.gradient.SobelGradientFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.gradient.SingleSobelGradientFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.hessian.SingleHessianFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.lipschitz.LipschitzFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.lipschitz.SingleLipschitzFeature.class);

	private static final Collection<Class<? extends FeatureOp>> general = Arrays.asList(
		net.imglib2.trainable_segmention.pixel_feature.filter.gradient.GradientFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.dog.DifferenceOfGaussiansFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.gradient.SingleGradientFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.gauss.SingleGaussFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.stats.SphereShapedFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.stats.SingleSphereShapedFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.identity.IdendityFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.gauss.GaussFeature.class,
		net.imglib2.trainable_segmention.pixel_feature.filter.dog.SingleDifferenceOfGaussiansFeature.class);

	@Test
	public void testAvialableFeatures2d() {
		GlobalSettings globals = GlobalSettings.default2dSettings();
		testAvailableFeatures(join(general, only2d), globals);
	}

	@Test
	public void tests3d() {
		GlobalSettings globals = GlobalSettings.default3dSettings();
		testAvailableFeatures(join(general, only3d), globals);
	}

	private void testAvailableFeatures(Collection<Class<? extends FeatureOp>> expected,
		GlobalSettings globals)
	{
		Map<String, Class<? extends FeatureOp>> f = AvailableFeatures.getMap(context, globals);
		Collection<Class<? extends FeatureOp>> features = f.values();
		assertContainsEquals(expected, features);
	}

	private <T> Set<T> join(Collection<T> a, Collection<T> b) {
		Set<T> result = new HashSet<>(a);
		result.addAll(b);
		return result;
	}

	private <T> void assertContainsEquals(Collection<T> expected, Collection<T> actual) {
		Set<T> toMany = new HashSet<>(actual);
		toMany.removeAll(expected);
		Set<T> missing = new HashSet<>(expected);
		missing.removeAll(actual);
		if (!toMany.isEmpty())
			fail("To many items: " + toMany);
		if (!missing.isEmpty())
			fail("Missing items: " + missing);
	}
}
