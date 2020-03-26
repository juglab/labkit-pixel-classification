
package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assume.assumeFalse;

/**
 * @author Matthias Arzt
 */
@RunWith(Parameterized.class)
public class BorderEffectsTest {

	public BorderEffectsTest(boolean useGpu) {
		this.useGpu = useGpu;
	}

	@Parameterized.Parameters(name = "useGpu = {0}")
	public static List<Boolean> data() {
		return Arrays.asList(false, true);
	}

	private final boolean useGpu;

	private Interval bigInterval = new FinalInterval(new long[] { 0, 0 }, new long[] { 150, 150 });

	private Interval interval = new FinalInterval(new long[] { 50, 50 }, new long[] { 100, 100 });

	private final Img<FloatType> fullImage = ImageJFunctions.convertFloat(Utils.loadImage(
		"bridge.png"));

	private RandomAccessibleInterval<FloatType> image = RevampUtils.copy(Utils.ops(), Views.interval(
		fullImage, bigInterval));

	@Test
	public void testGauss() {
		testFeature(GroupedFeatures.gauss());
	}

	@Test
	public void testHessian() {
		testFeature(GroupedFeatures.hessian());
	}

	@Deprecated
	@Test
	public void testGabor() {
		assumeFalse(useGpu);
		testFeature(GroupedFeatures.gabor());
	}

	@Test
	public void testDifferenceOfGaussians() {
		testFeature(GroupedFeatures.differenceOfGaussians());
	}

	@Test
	public void testLaplacian() {
		testFeature(GroupedFeatures.laplacian());
	}

	@Deprecated
	@Test
	public void testLipschitz() {
		assumeFalse(useGpu);
		testFeature(GroupedFeatures.lipschitz(50));
	}

	@Test
	public void testStatistics() {
		testFeature(GroupedFeatures.statistics());
	}

	public void testFeature(FeatureSetting feature) {
		FeatureCalculator calculator = FeatureCalculator.default2d().addFeature(feature).build();
		calculator.setUseGPU(useGpu);
		RandomAccessibleInterval<FloatType> expected = calculateExpected(calculator);
		RandomAccessibleInterval<FloatType> result = calculateResult(calculator);
		Utils.assertImagesEqual(120.0, result, expected);
	}

	public RandomAccessibleInterval<FloatType> calculateResult(FeatureCalculator feature) {
		return feature.apply(Views.extendBorder(image), interval);
	}

	public RandomAccessibleInterval<FloatType> calculateExpected(FeatureCalculator feature) {
		Interval featureInterval = RevampUtils.appendDimensionToInterval(interval, 0, feature.count() -
			1);
		return Views.interval(feature.apply(image), featureInterval);
	}
}
