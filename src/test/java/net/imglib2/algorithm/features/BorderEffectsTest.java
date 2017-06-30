package net.imglib2.algorithm.features;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public class BorderEffectsTest {

	private Interval bigInterval = new FinalInterval(new long[]{0, 0}, new long[]{150, 150});

	private Interval interval = new FinalInterval(new long[]{50, 50}, new long[]{100, 100});

	private final Img<FloatType> fullImage = ImageJFunctions.convertFloat(Utils.loadImage("bridge.png"));

	private RandomAccessibleInterval<FloatType> image = RevampUtils.copy(Views.interval(fullImage, bigInterval));

	@Test
	public void testGauss() {
		testFeature(GaussFeature.group());
	}

	@Test
	public void testHessian() {
		testFeature(HessianFeature.group());
	}

	@Test
	public void testGabor() {
		testFeature(GaborFeature.group());
	}

	@Test
	public void testDifferenceOfGaussians() {
		testFeature(DifferenceOfGaussiansFeature.group());
	}

	@Test
	public void testGradient() { testFeature(GradientFeature.group()); }

	@Test
	public void testLipschitz() {
		testFeature(LipschitzFeature.group(50));
	}

	@Test
	public void testMin() { testFeature(ShapedFeature.min()); }

	@Test
	public void testMax() { testFeature(ShapedFeature.max()); }

	@Test
	public void testMean() { testFeature(ShapedFeature.mean()); }

	@Test
	public void testMedian() { testFeature(ShapedFeature.median()); }

	@Test
	public void testVariance() { testFeature(ShapedFeature.variance()); }

	public void testFeature(Feature feature) {
		RandomAccessibleInterval<FloatType> expected = calculateExpected(feature);
		RandomAccessibleInterval<FloatType> result = calculateResult(feature);
		Utils.assertImagesEqual(50.0, result, expected);
	}

	public void showDifference(Feature feature) {
		RandomAccessibleInterval<FloatType> expected = calculateExpected(feature);
		RandomAccessibleInterval<FloatType> result = calculateResult(feature);
		Utils.showPsnr(expected, result);
		Utils.show(Utils.subtract(expected, result), expected, result);
	}

	public RandomAccessibleInterval<FloatType> calculateResult(Feature feature) {
		Interval featureInterval = RevampUtils.extend(interval, 0, feature.count() - 1);
		RandomAccessibleInterval<FloatType> result = RevampUtils.ops().create().img(featureInterval, new FloatType());
		feature.apply(image, RevampUtils.slices(result));
		return result;
	}

	public RandomAccessibleInterval<FloatType> calculateExpected(Feature feature) {
		Interval featureInterval = RevampUtils.extend(interval, 0, feature.count() - 1);
		return Views.interval(Features.applyOnImg(feature, image), featureInterval);
	}

	public void showPsnrs() {
		Feature feature = new FeatureGroup(GaussFeature.group(), HessianFeature.group(), GaussFeature.group(),
				DifferenceOfGaussiansFeature.group(), GradientFeature.group(), LipschitzFeature.group(50),
				ShapedFeature.min(), ShapedFeature.max(), ShapedFeature.mean(), ShapedFeature.median(),
				ShapedFeature.variance());
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

	public static void mainDisabled(String... args) {
		new BorderEffectsTest().showDifference(GaborFeature.group());
	}
}
