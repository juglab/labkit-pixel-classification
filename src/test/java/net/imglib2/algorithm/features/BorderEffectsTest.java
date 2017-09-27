package net.imglib2.algorithm.features;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
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

	private GroupedFeatures groupedFeatures = new GroupedFeatures(Utils.ops());

	private Interval bigInterval = new FinalInterval(new long[]{0, 0}, new long[]{150, 150});

	private Interval interval = new FinalInterval(new long[]{50, 50}, new long[]{100, 100});

	private final Img<FloatType> fullImage = ImageJFunctions.convertFloat(Utils.loadImage("bridge.png"));

	private RandomAccessibleInterval<FloatType> image = RevampUtils.copy(Utils.ops(), Views.interval(fullImage, bigInterval));

	@Test
	public void testGauss() {
		testFeature(groupedFeatures.gauss());
	}

	@Test
	public void testHessian() {
		testFeature(groupedFeatures.hessian());
	}

	@Test
	public void testGabor() {
		testFeature(groupedFeatures.gabor());
	}

	@Test
	public void testDifferenceOfGaussians() {
		testFeature(groupedFeatures.differenceOfGaussians());
	}

	@Test
	public void testGradient() { testFeature(groupedFeatures.sobelGradient()); }

	@Test
	public void testLipschitz() {
		testFeature(groupedFeatures.lipschitz(50));
	}

	@Test
	public void testMin() { testFeature(groupedFeatures.min()); }

	@Test
	public void testMax() { testFeature(groupedFeatures.max()); }

	@Test
	public void testMean() { testFeature(groupedFeatures.mean()); }

	@Test
	public void testMedian() { testFeature(groupedFeatures.median()); }

	@Test
	public void testVariance() { testFeature(groupedFeatures.variance()); }

	public void testFeature(FeatureOp feature) {
		GrayFeatureGroup group = Features.grayGroup(feature);
		RandomAccessibleInterval<FloatType> expected = calculateExpected(group);
		RandomAccessibleInterval<FloatType> result = calculateResult(group);
		Utils.assertImagesEqual(50.0, result, expected);
	}

	public void showDifference(FeatureOp feature) {
		GrayFeatureGroup group = Features.grayGroup(feature);
		RandomAccessibleInterval<FloatType> expected = calculateExpected(group);
		RandomAccessibleInterval<FloatType> result = calculateResult(group);
		Utils.showPsnr(expected, result);
		Utils.show(Utils.subtract(expected, result), expected, result);
	}

	public RandomAccessibleInterval<FloatType> calculateResult(GrayFeatureGroup feature) {
		Interval featureInterval = RevampUtils.extend(interval, 0, feature.count() - 1);
		RandomAccessibleInterval<FloatType> result = Utils.ops().create().img(featureInterval, new FloatType());
		feature.apply(image, RevampUtils.slices(result));
		return result;
	}

	public RandomAccessibleInterval<FloatType> calculateExpected(GrayFeatureGroup feature) {
		Interval featureInterval = RevampUtils.extend(interval, 0, feature.count() - 1);
		return Views.interval(Features.applyOnImg(feature, image), featureInterval);
	}

	public void showPsnrs() {
		GrayFeatureGroup feature = Features.grayGroup(groupedFeatures.gauss(), groupedFeatures.hessian(), groupedFeatures.gauss(),
				groupedFeatures.differenceOfGaussians(), groupedFeatures.sobelGradient(), groupedFeatures.lipschitz(50),
				groupedFeatures.min(), groupedFeatures.max(), groupedFeatures.mean(), groupedFeatures.median(),
				groupedFeatures.variance());
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
