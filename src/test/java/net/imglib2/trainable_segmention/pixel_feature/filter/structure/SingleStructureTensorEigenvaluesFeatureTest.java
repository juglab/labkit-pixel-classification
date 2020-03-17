
package net.imglib2.trainable_segmention.pixel_feature.filter.structure;

import ij.ImagePlus;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;
import trainableSegmentation.ImageScience;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link SingleStructureTensorEigenvaluesFeature}.
 */
public class SingleStructureTensorEigenvaluesFeatureTest {

	private final double sigma = 2.0;
	private final double integrationScale = 5.0;

	private final FeatureCalculator calculator3d = FeatureCalculator.default2d().dimensions(3)
		.addFeature(SingleFeatures.structureTensor(sigma, integrationScale)).build();

	private final FeatureCalculator calculator2d = FeatureCalculator.default2d().dimensions(2)
		.addFeature(SingleFeatures.structureTensor(sigma, integrationScale)).build();

	@Test
	public void testApply3d() {
		RandomAccessible<Localizable> positions = Localizables.randomAccessible(3);
		Converter<Localizable, FloatType> converter = (position, o) -> {
			double x = position.getDoublePosition(0);
			double y = position.getDoublePosition(1);
			double z = position.getDoublePosition(2);
			o.setReal(x + 2 * y + 3 * z);
		};
		RandomAccessible<FloatType> input = Converters.convert(positions, converter, new FloatType());
		RandomAccessibleInterval<FloatType> result = calculator3d.apply(input, new FinalInterval(1, 1,
			1));
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 14, 0, 0 }, 1, 1,
			1, 3);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.00001);
	}

	@Test
	public void testAttributeLabels3d() {
		List<String> attributeLabels = calculator3d.attributeLabels();
		List<String> expected = Arrays.asList(
			"structure tensor - largest eigenvalue sigma=2.0 integrationScale=5.0",
			"structure tensor - middle eigenvalue sigma=2.0 integrationScale=5.0",
			"structure tensor - smallest eigenvalue sigma=2.0 integrationScale=5.0");
		assertEquals(expected, attributeLabels);
	}

	@Test
	public void testApply2d() {
		RandomAccessible<Localizable> positions = Localizables.randomAccessible(2);
		Converter<Localizable, FloatType> converter = (position, o) -> {
			double x = position.getDoublePosition(0);
			double y = position.getDoublePosition(1);
			o.setReal(x + 2 * y);
		};
		RandomAccessible<FloatType> input = Converters.convert(positions, converter, new FloatType());
		RandomAccessibleInterval<FloatType> result = calculator2d.apply(input, new FinalInterval(1, 1));
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 5, 0 }, 1, 1, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.00001);
	}

	@Test
	public void testAttributeLabels2d() {
		List<String> attributeLabels = calculator2d.attributeLabels();
		List<String> expected = Arrays.asList(
			"structure tensor - largest eigenvalue sigma=2.0 integrationScale=5.0",
			"structure tensor - smallest eigenvalue sigma=2.0 integrationScale=5.0");
		assertEquals(expected, attributeLabels);
	}

	@Test
	public void testCompareToImageScienceLibrary() {
		Img<FloatType> image = ArrayImgs.floats(100, 100, 100);
		Views.interval(image, new FinalInterval(50, 50, 50)).forEach(FloatType::setOne);
		Interval target = Intervals.createMinSize(40, 40, 40, 0, 20, 20, 20, 3);
		IntervalView<FloatType> output = createImage(target);
		calculator3d.apply(Views.extendBorder(image), RevampUtils.slices(output));
		RandomAccessibleInterval<FloatType> result2 = UsingImagePlus.asRAI(ImageScience.computeEigenimages(sigma,
			integrationScale, UsingImagePlus.asImagePlusXYZ(image)));
		Utils.assertImagesEqual(39, Views.interval(result2, target), output);
		// ImageScience calculates derivatives with slightly to low intensity.
		// The PSNR is much better, when results are normalized to compensate the
		// differently scaled intensities.
		Utils.assertImagesEqual(55, normalize(output), normalize(Views.interval(result2, target)));
	}

	private RandomAccessibleInterval<FloatType> normalize(RandomAccessibleInterval<FloatType> image) {
		double variance = 0;
		for (RealType<?> pixel : Views.iterable(image))
			variance += square(pixel.getRealDouble());
		variance /= Intervals.numElements(image);
		double factor = 1 / Math.sqrt(variance);
		return scale(factor, image);
	}

	private RandomAccessibleInterval<FloatType> scale(double factor,
		RandomAccessibleInterval<FloatType> image)
	{
		return Converters.convert(image, (i, o) -> {
			o.set(i);
			o.mul(factor);
		}, new FloatType());
	}

	private static double square(double value) {
		return value * value;
	}

	private static IntervalView<FloatType> createImage(Interval target) {
		return Views.translate(ArrayImgs.floats(Intervals.dimensionsAsLongArray(target)), Intervals
			.minAsLongArray(target));
	}

	private static class UsingImagePlus {

		private static ImagePlus asImagePlusXYZ(Img<FloatType> image) {
			ImagePlus imp = ImageJFunctions.wrap(image, "").duplicate();
			imp.setStack(imp.getStack(), 1, imp.getStack().size(), 1);
			return imp;
		}

		private static RandomAccessibleInterval<FloatType> asRAI(ArrayList<ImagePlus> result) {
			return Views.stack(result.stream().map(ImageJFunctions::wrapFloat).collect(Collectors
				.toList()));
		}
	}
}
