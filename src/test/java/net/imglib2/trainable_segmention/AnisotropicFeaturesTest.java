
package net.imglib2.trainable_segmention;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
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

public class AnisotropicFeaturesTest {

	private static OpService ops = new Context().service(OpService.class);

	@Test
	public void test() {
//		//testAnisotropy(GroupedFeatures.sobelGradient()); // can be removed
//		testAnisotropy(GroupedFeatures.gradient());
//		testAnisotropy(GroupedFeatures.hessian3D(false));
//		//testAnisotropy(GroupedFeatures.hessian()); // test it
//		testAnisotropy(GroupedFeatures.differenceOfGaussians());
//		//testAnisotropy(GroupedFeatures.gabor()); // It's only 2D. Performance could be improved. Good gabor filter would requir steerable filters.
//		// Gabor can be easily capable of anisotropy because, there is already parameter.
//		testAnisotropy(GroupedFeatures.gauss());
//		testAnisotropy(GroupedFeatures.laplacian());
		// testAnisotropy(GroupedFeatures.legacyGabor()); // should be removed
		testAnisotropy(GroupedFeatures.structure());
//		testAnisotropy(SingleFeatures.lipschitz(0.01, 20));
//		testAnisotropy(GroupedFeatures.min()); // make them anistropic by implementing, hyper elipsoid spherte
//		testAnisotropy(GroupedFeatures.max());
//		testAnisotropy(GroupedFeatures.median());
//		testAnisotropy(GroupedFeatures.variance());
//		testAnisotropy(GroupedFeatures.mean());
	}

	private void testAnisotropy(FeatureSetting setting) {
		FeatureSettings settings = new FeatureSettings(GlobalSettings.default3d().build(), setting);
		RandomAccessibleInterval<DoubleType> image = testImage();
		RandomAccessibleInterval<DoubleType> scaleImage = Views.subsample(image, 1, 1, 2);

		FeatureCalculator calculator = new FeatureCalculator(ops, settings);
		RandomAccessibleInterval<FloatType> result = calculator.apply(Views.extendBorder(image),
			Intervals.createMinSize(24, 24, 24, 50, 50, 50));
		RandomAccessibleInterval<FloatType> scaledFeautres = Views.subsample(result, 1, 1, 2, 1);

		FeatureCalculator calculator2 = new FeatureCalculator(ops, settings);
		calculator2.setPixelSize(1, 1, 2);
		RandomAccessibleInterval<FloatType> scaledImagesFeatures = calculator2.apply(Views.extendBorder(
			scaleImage), Intervals.createMinSize(24, 24, 12, 50, 50, 25));
		Utils.assertImagesEqual(40, scaledFeautres, Views.zeroMin(scaledImagesFeatures));
	}

	private static Img<DoubleType> testImage() {
		Img<DoubleType> image = ArrayImgs.doubles(100, 100, 100);
		long scale = 10;
		RandomImgs.seed(1).randomize(Views.subsample(image, scale));
		Gauss3.gauss(scale, Views.extendZero(image), image);
		return image;
	}
}
