
package net.imglib2.trainable_segmention.pixel_feature.filter.structure;

import ij.ImagePlus;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.EigenValuesSymmetric3D;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;
import org.junit.Test;
import org.scijava.Context;
import trainableSegmentation.ImageScience;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests {@link SingleStructureFeature3D}.
 */
public class SingleStructureFeature3DTest {

	private final OpService ops = new Context().service(OpService.class);

	@Test
	public void testCompareToImageScienceLibrary() {
		double sigma = 2.0;
		double integrationScale = 5.0;
		Img<FloatType> image = ArrayImgs.floats(100, 100, 100);
		Views.interval(image, new FinalInterval(50, 50, 50)).forEach(FloatType::setOne);
		FeatureOp feature = SingleFeatures.structure(sigma, integrationScale).newInstance(ops,
			GlobalSettings.default3d().build());
		Interval target = Intervals.createMinSize(40, 40, 40, 0, 20, 20, 20, 3);
		IntervalView<FloatType> output = createImage(target);
		feature.apply(image, RevampUtils.slices(output));
		RandomAccessibleInterval<FloatType> result2 = asRAI(ImageScience.computeEigenimages(sigma,
			integrationScale, asImagePlusXYZ(image)));
		Utils.assertImagesEqual(40, output, Views.interval(result2, target));
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

	private ImagePlus asImagePlusXYZ(Img<FloatType> image) {
		ImagePlus imp = ImageJFunctions.wrap(image, "").duplicate();
		imp.setStack(imp.getStack(), 1, imp.getStack().size(), 1);
		return imp;
	}

	private RandomAccessibleInterval<FloatType> asRAI(ArrayList<ImagePlus> result) {
		return Views.stack(result.stream().map(ImageJFunctions::wrapFloat).collect(Collectors
			.toList()));
	}

	private static IntervalView<FloatType> createImage(Interval target) {
		return Views.translate(ArrayImgs.floats(Intervals.dimensionsAsLongArray(target)), Intervals
			.minAsLongArray(target));
	}
}
