
package net.imglib2.trainable_segmention;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;
import preview.net.imglib2.algorithm.gauss3.Gauss3;
import preview.net.imglib2.loops.LoopBuilder;
import preview.net.imglib2.parallel.Parallelization;

public class AnisotropicFeaturesTest {

	private static OpService ops = new Context().service(OpService.class);

	@Test
	public void testGradient() {
		testAnisotropy(GroupedFeatures.gradient());
	}

	@Test
	public void testHessian() {
		testAnisotropy2d(39, GroupedFeatures.hessian());
		testAnisotropy3d(GroupedFeatures.hessian());
	}

	@Test
	public void testGauss() {
		testAnisotropy(GroupedFeatures.gauss());
	}

	@Test
	public void testDifferenceOfGaussians() {
		testAnisotropy(GroupedFeatures.differenceOfGaussians());
	}

	@Test
	public void testLaplacian() {
		testAnisotropy(GroupedFeatures.laplacian());
	}

	@Test
	public void testStructure() {
		testAnisotropy3d(GroupedFeatures.structureTensor());
	}

	@Test
	public void testLipschitz() {
		testAnisotropy(SingleFeatures.lipschitz(0.01, 20));
	}

	@Test
	public void testStatistics() {
		testAnisotropy(GroupedFeatures.statistics());
	}

	private void testAnisotropy(FeatureSetting setting) {
		testAnisotropy2d(setting);
		testAnisotropy3d(setting);
	}

	private void testAnisotropy3d(FeatureSetting setting) {
		FeatureSettings settings = new FeatureSettings(GlobalSettings.default3d().build(), setting);
		RandomAccessibleInterval<DoubleType> image = testImage(100, 100, 100);
		RandomAccessibleInterval<DoubleType> scaleImage = Views.subsample(image, 1, 1, 2);

		FeatureCalculator calculator = FeatureCalculator.default2d()
			.ops(ops)
			.dimensions(3)
			.addFeature(setting)
			.build();
		RandomAccessibleInterval<FloatType> result = calculator.apply(Views.extendBorder(image),
			Intervals.createMinSize(24, 24, 24, 50, 50, 50));
		RandomAccessibleInterval<FloatType> scaledFeatures = Views.subsample(result, 1, 1, 2, 1);

		FeatureCalculator calculator2 = FeatureCalculator.default2d()
			.ops(ops)
			.dimensions(3)
			.addFeature(setting)
			.pixelSize(1, 1, 2)
			.build();
		RandomAccessibleInterval<FloatType> scaledImagesFeatures = calculator2.apply(Views.extendBorder(
			scaleImage), Intervals.createMinSize(24, 24, 12, 50, 50, 25));
		Utils.assertImagesEqual(40, scaledFeatures, Views.zeroMin(scaledImagesFeatures));
	}

	private void testAnisotropy2d(FeatureSetting setting) {
		testAnisotropy2d(40, setting);
	}

	private void testAnisotropy2d(double expectedPsnr, FeatureSetting setting) {
		RandomAccessibleInterval<DoubleType> image = testImage(100, 100);
		RandomAccessibleInterval<DoubleType> scaleImage = Views.subsample(image, 1, 2);

		FeatureCalculator calculator = FeatureCalculator.default2d()
			.ops(ops)
			.addFeature(setting)
			.build();
		RandomAccessibleInterval<FloatType> result = calculator.apply(Views.extendBorder(image),
			Intervals.createMinSize(24, 24, 50, 50));
		RandomAccessibleInterval<FloatType> scaledFeatures = Views.subsample(result, 1, 2, 1);

		FeatureCalculator calculator2 = FeatureCalculator.default2d()
			.ops(ops)
			.addFeature(setting)
			.pixelSize(1, 2)
			.build();
		RandomAccessibleInterval<FloatType> scaledImagesFeatures = calculator2.apply(Views.extendBorder(
			scaleImage), Intervals.createMinSize(24, 12, 50, 25));
		Utils.assertImagesEqual(expectedPsnr, scaledFeatures, Views.zeroMin(scaledImagesFeatures));
	}

	private static Img<DoubleType> testImage(long... size) {
		Img<DoubleType> image = ArrayImgs.doubles(size);
		long scale = 10;
		RandomImgs.seed(1).randomize(Views.subsample(image, scale));
		Gauss3.gauss(scale, Views.extendZero(image), image);
		return image;
	}

	@Test
	public void testFeatureInputAnisotropy() {
		Parallelization.runSingleThreaded(() -> {
			Img<DoubleType> image = testImage(100, 100);
			RandomAccessibleInterval<DoubleType> anisotropicImage = Views.subsample(image, 1, 2);
			RandomAccessibleInterval<FloatType> copy = copy(RealTypeConverters.convert(image,
				new FloatType()));
			FeatureInput featureInput = new FeatureInput(copy, Intervals.createMinSize(24, 24, 50, 50),
				new double[] { 1, 1 });
			FeatureInput anisotropicFeatureInput = new FeatureInput(RealTypeConverters.convert(
				anisotropicImage, new FloatType()), Intervals.createMinSize(24, 12, 50, 25), new double[] {
					1, 2 });
			RandomAccessibleInterval<DoubleType> derivativeInY = Views.zeroMin(Views.subsample(
				featureInput.derivedGauss(1.0, 0, 2), 1, 2));
			RandomAccessibleInterval<DoubleType> anisotropicDerivativeInY = Views.zeroMin(
				anisotropicFeatureInput.derivedGauss(1.0, 0, 2));
			Utils.assertImagesEqual(40, derivativeInY, anisotropicDerivativeInY);
		});
	}

	private RandomAccessibleInterval<FloatType> copy(RandomAccessibleInterval<FloatType> input) {
		Img<FloatType> output = ArrayImgs.floats(Intervals.dimensionsAsLongArray(input));
		LoopBuilder.setImages(input, output).forEachPixel((i, o) -> o.set(i));
		return output;
	}
}
