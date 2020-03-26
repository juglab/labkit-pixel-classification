
package net.imglib2.trainable_segmention.pixel_feature.filter;

import ij.ImagePlus;
import ij.ImageStack;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
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

	private Img<FloatType> img = initSample();

	private Img<FloatType> initSample() {
		Img<FloatType> img = ArrayImgs.floats(new long[] { 50, 50, 50 });
		String equation = "Math.sin(0.1 * p[0]) + Math.sin(0.2 * p[1]) + Math.cos(0.3 * p[2])";
		ops.image().equation(img, equation);
		return img;
	}

	@Test
	public void testGaussian() {
		testFeatureIgnoreAttributes(59, FeatureStack3D.GAUSSIAN, GroupedFeatures.gauss());
	}

	@Test
	public void testLaplacian() {
		testFeatureIgnoreAttributes(45, FeatureStack3D.LAPLACIAN, GroupedFeatures.laplacian());
	}

	@Test
	public void testStructure() {
		testFeatureIgnoreAttributes(45, FeatureStack3D.STRUCTURE, GroupedFeatures.structureTensor());
	}

	@Test
	public void testGradient() {
		testFeatureIgnoreAttributes(44, FeatureStack3D.EDGES, GroupedFeatures.gradient());
	}

	private void testFeature(int expectedPsnr, int featureID, FeatureSetting featureSetting) {
		FeatureStackArray fsa = calculateFeatureStack(featureID);
		FeatureCalculator group = setupFeatureCalculator(featureSetting);
		testAttributes(fsa, group);
		testFeatures(expectedPsnr, fsa, group);
	}

	private void testFeatureIgnoreAttributes(int expectedPsnr, int featureID,
		FeatureSetting featureSetting)
	{
		FeatureStackArray fsa = calculateFeatureStack(featureID);
		FeatureCalculator group = setupFeatureCalculator(featureSetting);
		testFeatures(expectedPsnr, fsa, group);
	}

	private void testFeatures(int expectedPsnr, FeatureStackArray fsa, FeatureCalculator group) {
		RandomAccessibleInterval<FloatType> expected = stripOriginalAndBorder(getImage(fsa));
		RandomAccessibleInterval<FloatType> result = stripOriginalAndBorder(group.apply(img));
		Utils.assertImagesEqual(expectedPsnr, expected, result);
	}

	private void testAttributes(FeatureStackArray fsa, FeatureCalculator group) {
		List<String> expectedLabels = oldAttributes(fsa);
		List<String> actualLabels = getAttributeLabels(group);
		assertEquals(expectedLabels, actualLabels);
	}

	private RandomAccessibleInterval<FloatType> stripOriginalAndBorder(
		RandomAccessibleInterval<FloatType> image)
	{
		long[] min = Intervals.minAsLongArray(image);
		long[] max = Intervals.maxAsLongArray(image);
		assertEquals(4, image.numDimensions());
		for (int i = 0; i < 3; i++) {
			min[i] += 20;
			max[i] -= 20;
		}
		min[3]++;
		return Views.interval(image, new FinalInterval(min, max));
	}

	private FeatureCalculator setupFeatureCalculator(FeatureSetting featureSetting) {
		FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default3d().build(), Arrays
			.asList(SingleFeatures.identity(), featureSetting));
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
		FeatureStack3D stack = new FeatureStack3D(ImageJFunctions.wrap(img, "sample").duplicate());
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
		return ImageJFunctions.wrap(imagePlus);
	}
}
