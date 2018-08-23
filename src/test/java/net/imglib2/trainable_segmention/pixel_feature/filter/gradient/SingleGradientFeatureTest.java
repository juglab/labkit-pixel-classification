package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import ij.ImagePlus;
import ij.ImageStack;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;
import trainableSegmentation.FeatureStack3D;
import trainableSegmentation.FeatureStackArray;

public class SingleGradientFeatureTest {

	private final long[] dimensions = {100, 100, 100};
	private final Context context = new Context();
	private final OpService ops = context.service(OpService.class);
	private final Img<FloatType> img = initSample();
	private final ImagePlus image = ImageJFunctions.wrap(img, "input");

	private Img<FloatType> initSample() {
		Img<FloatType> img = ArrayImgs.floats(dimensions);
		Views.interval(img, Intervals.createMinSize(40,50,45,10,20,30)).forEach(x -> x.setOne());
		return img;
	}
//	private Img<FloatType> initSample() {
//		Img<FloatType> img = ArrayImgs.floats(dimensions);
//		String equation = "Math.sin(2 * p[0]) + Math.sin(1 * p[1]) + Math.cos(5 * p[2])";
//		ops.image().equation(img, equation);
//		return img;
//	}

	@Test
	public void testImage() {
		RandomAccessibleInterval<FloatType> expected = getExpected(10);
		RandomAccessibleInterval<FloatType> result = getActual(2.0, 4);
		Utils.assertImagesEqual(150, expected, result);
	}

	@Test
	public void testGroup() {

	}

	public static void main(String... args) {
		new SingleGradientFeatureTest().run();
	}

	private void run() {
		new ij.ImageJ();
		Utils.showDifference( getExpected(10), getActual(2.0, 4));
	}

	private RandomAccessibleInterval<FloatType> getActual(double sigma, int order) {
		final FeatureSetting featureSetting = new FeatureSetting(SingleDerivativesFeature.class, "sigma", sigma, "order", order);
		FeatureCalculator fc = new FeatureCalculator(ops, new FeatureSettings(GlobalSettings.default3dSettings(), featureSetting));
		return Views.hyperSlice(fc.apply(img), 3, 0);
	}

	private RandomAccessibleInterval<FloatType> getExpected(int n) {
		boolean[] enabledFeatures = new boolean[FeatureStack3D.availableFeatures.length];
		enabledFeatures[FeatureStack3D.DERIVATIVES] = true;
		FeatureStack3D stack = new FeatureStack3D(image.duplicate());
		stack.setEnableFeatures(enabledFeatures);
		stack.updateFeaturesMT();
		FeatureStackArray result = stack.getFeatureStackArray();
		return ImageJFunctions.wrapFloat( extractFeature(result, n) );
	}

	private ImagePlus extractFeature(FeatureStackArray array, int n) {
		ImageStack stack = new ImageStack(array.getWidth(), array.getHeight());
		for (int i = 0; i < array.getSize(); i++)
			stack.addSlice(array.get(i).getStack().getProcessor(n));
		final String sliceLabel = array.get(0).getStack().getSliceLabel(n);
		System.out.println(sliceLabel);
		return new ImagePlus(sliceLabel, stack);
	}
}
