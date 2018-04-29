package net.imglib2.trainable_segmention.pixel_feature.filter;

import ij.ImagePlus;
import ij.ImageStack;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.GradientFeature3d;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.Hessian3DFeature;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.Context;
import trainableSegmentation.FeatureStack3D;
import trainableSegmentation.FeatureStackArray;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class FeatureStack3DTest {

	private Context context = new Context();
	private OpService ops = context.service(OpService.class);
	private Img<DoubleType> img = initSample();
	private ImagePlus image = ImageJFunctions.wrap(img, "input");

	private Img<DoubleType> initSample() {
		Img<DoubleType> img = ops.create().img(new long[]{20, 20, 20});
		String equation = "Math.sin(2 * p[0]) + Math.sin(1 * p[1]) + Math.cos(5 * p[2])";
		//String equation = "p[0] * p[0] + p[1] * p[1] + p[2] * p[2]";
		ops.image().equation(img, equation);
		return img;
	}

	@Test
	public void testHessian() {
		testFeature(FeatureStack3D.HESSIAN, Hessian3DFeature.class);
	}

	@Ignore("not implemented yet")
	@Test
	public void testDerivatives() {
		testFeature(FeatureStack3D.DERIVATIVES, GradientFeature3d.class);
	}

	private void testFeature(int featureID, Class<? extends FeatureOp> featureClass) {
		FeatureStackArray fsa = calculateFeatureStack(featureID);
		FeatureCalculator group = setupFeatureCalculator(FeatureSetting.fromClass(featureClass));
		List<String> expectedLabels = oldAttributes(fsa);
		List<String> actualLabels = getAttributeLabels(group);
		assertEquals(expectedLabels, actualLabels);
		RandomAccessibleInterval<FloatType> expected = getImage(fsa);
		RandomAccessibleInterval<FloatType> result = group.apply(img);
		Utils.assertImagesEqual(40, expected, result);
	}

	private FeatureCalculator setupFeatureCalculator(FeatureSetting featureSetting) {
		FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default3dSettings(), Arrays.asList(SingleFeatures.identity(), featureSetting));
		return new FeatureCalculator(Utils.ops(), featureSettings);
	}

	private RandomAccessibleInterval<FloatType> getImage(FeatureStackArray fsa) {
		List<Img<FloatType>> slices = IntStream.range(0, fsa.getSize())
				.mapToObj(index -> stackToImg(fsa.get(index).getStack(), Integer.toString(index)))
				.collect(Collectors.toList());
		return Views.permute(Views.stack(slices), 2, 3);
	}

	private FeatureStackArray calculateFeatureStack(int featureIndex) {
		boolean[] enabledFeatures = new boolean[FeatureStack3D.availableFeatures.length];
		enabledFeatures[featureIndex] = true;
		FeatureStack3D stack = new FeatureStack3D(image.duplicate());
		stack.setEnableFeatures(enabledFeatures);
		stack.updateFeaturesMT();
		return stack.getFeatureStackArray();
	}

	private List<String> getAttributeLabels(FeatureCalculator group) {
		return group.attributeLabels();
	}

	private List<String> oldAttributes(FeatureStackArray fsa) {
		return FeatureStackTest.oldAttributes(fsa.get(0));
	}

	private Img<FloatType> stackToImg(ImageStack stack1, String title) {
		ImagePlus imagePlus = new ImagePlus(title, stack1);
		imagePlus.show();
		return ImageJFunctions.wrap(imagePlus);
	}

	public static void main(String... args) {
		new FeatureStack3DTest().run();
	}

	private void run() {
		new ij.ImageJ();
		FeatureStackArray fsa = calculateFeatureStack(FeatureStack3D.DERIVATIVES);
		FeatureCalculator group = setupFeatureCalculator(FeatureSetting.fromClass(GradientFeature3d.class));
		List<String> expectedLabels = oldAttributes(fsa);
		List<String> actualLabels = getAttributeLabels(group);
		assertEquals(expectedLabels, actualLabels);
		RandomAccessibleInterval<FloatType> expected = getImage(fsa);
		RandomAccessibleInterval<FloatType> result = group.apply(img);
		Utils.assertImagesEqual(200, expected, result);
	}
}
