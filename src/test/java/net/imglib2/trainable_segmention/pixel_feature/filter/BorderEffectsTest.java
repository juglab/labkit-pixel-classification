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
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class BorderEffectsTest {

	private Interval bigInterval = new FinalInterval(new long[]{0, 0}, new long[]{150, 150});

	private Interval interval = new FinalInterval(new long[]{50, 50}, new long[]{100, 100});

	private final Img<FloatType> fullImage = ImageJFunctions.convertFloat(Utils.loadImage("bridge.png"));

	private RandomAccessibleInterval<FloatType> image = RevampUtils.copy(Utils.ops(), Views.interval(fullImage, bigInterval));

	@Test
	public void testGauss() {
		testFeature(GroupedFeatures.gauss());
	}

	@Test
	public void testHessian() {
		testFeature(GroupedFeatures.hessian());
	}

	@Test
	public void testGabor() {
		testFeature(GroupedFeatures.gabor());
	}

	@Test
	public void testDifferenceOfGaussians() {
		testFeature(GroupedFeatures.differenceOfGaussians());
	}

	@Test
	public void testGradient() { testFeature(GroupedFeatures.sobelGradient()); }

	@Test
	public void testLipschitz() {
		testFeature(GroupedFeatures.lipschitz(50));
	}

	@Test
	public void testMin() { testFeature(GroupedFeatures.min()); }

	@Test
	public void testMax() { testFeature(GroupedFeatures.max()); }

	@Test
	public void testMean() { testFeature(GroupedFeatures.mean()); }

	@Test
	public void testMedian() { testFeature(GroupedFeatures.median()); }

	@Test
	public void testVariance() { testFeature(GroupedFeatures.variance()); }

	public void testFeature(FeatureSetting feature) {
		FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default2d().build(), Arrays.asList(feature));
		FeatureCalculator group = new FeatureCalculator(Utils.ops(), featureSettings);
		RandomAccessibleInterval<FloatType> expected = calculateExpected(group);
		RandomAccessibleInterval<FloatType> result = calculateResult(group);
		Utils.assertImagesEqual(50.0, result, expected);
	}

	public void showDifference(FeatureSetting feature) {
		FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default2d().build(), Arrays.asList(feature));
		FeatureCalculator group = new FeatureCalculator(Utils.ops(), featureSettings);
		RandomAccessibleInterval<FloatType> expected = calculateExpected(group);
		RandomAccessibleInterval<FloatType> result = calculateResult(group);
		Utils.showPsnr(expected, result);
		Utils.show(Utils.subtract(expected, result), expected, result);
	}

	public RandomAccessibleInterval<FloatType> calculateResult(FeatureCalculator feature) {
		Interval featureInterval = RevampUtils.appendDimensionToInterval(interval, 0, feature.count() - 1);
		RandomAccessibleInterval<FloatType> result = Utils.ops().create().img(featureInterval, new FloatType());
		feature.apply(image, RevampUtils.slices(result));
		return result;
	}

	public RandomAccessibleInterval<FloatType> calculateExpected(FeatureCalculator feature) {
		Interval featureInterval = RevampUtils.appendDimensionToInterval(interval, 0, feature.count() - 1);
		return Views.interval(feature.apply(image), featureInterval);
	}

	public void showPsnrs() {
		FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default2d().build(), Arrays.asList(GroupedFeatures.gauss(), GroupedFeatures.hessian(), GroupedFeatures.gauss(), GroupedFeatures.differenceOfGaussians(), GroupedFeatures.sobelGradient(), GroupedFeatures.lipschitz(50), GroupedFeatures.min(), GroupedFeatures.max(), GroupedFeatures.mean(), GroupedFeatures.median(), GroupedFeatures.variance()));
		FeatureCalculator feature = new FeatureCalculator(Utils.ops(), featureSettings);
		RandomAccessibleInterval<FloatType> allResults = calculateResult(feature);
		RandomAccessibleInterval<FloatType> allExpected = calculateExpected(feature);
		int axis = image.numDimensions();
		List<String> attributes = feature.attributeLabels();
		for(int i = 0; i < feature.count(); i++) {
			RandomAccessibleInterval<FloatType> result = Views.hyperSlice(allResults, axis, i);
			RandomAccessibleInterval<FloatType> expected = Views.hyperSlice(allExpected, axis, i);
			String attribute = attributes.get(i);
			System.out.println("Attribute: " + attribute + "   PSNR: " + Utils.psnr(expected, result));
		}
	}
}
