package net.imglib2.algorithm.features;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.Context;
import trainableSegmentation.FeatureStack;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class FeatureStackTest {

	private static ImagePlus bridgeImage = Utils.loadImage("nuclei.tif");

	private static Img<FloatType> bridgeImg = ImagePlusAdapter.convertFloat(bridgeImage);
	private OpService ops = new Context(OpService.class).service(OpService.class);

	public static void main(String... args) {
		Img<FloatType> img = ImageJFunctions.convertFloat(squarePictureWithCenteredDot());
		RandomAccessibleInterval<FloatType> result = Features.applyOnImg(LipschitzFeature.group(0), img);
		ImageJFunctions.show(result);
	}

	public static RandomAccessibleInterval<FloatType> createStack(RandomAccessibleInterval<FloatType> image, Feature feature) {
		return Features.applyOnImg(new FeatureGroup(new IdendityFeature(), feature), image);
	}

	@Test
	public void testEmptyStack() {
		RandomAccessibleInterval<FloatType> expected = generateFeatures(bridgeImage, nCopiesAsArray(20, false));
		RandomAccessibleInterval<FloatType> result = createEmptyStack(bridgeImg);
		Utils.assertImagesEqual(expected, result);
	}

	private RandomAccessibleInterval<FloatType> createEmptyStack(Img<FloatType> image) {
		return image;
	}

	@Test
	public void testGaussStack() {
		testFeature(40, FeatureStack.GAUSSIAN, GaussFeature.group());
	}

	private void testFeature(float expectedPsnr, int oldFeatureId, Feature newFeature) {
		RandomAccessibleInterval<FloatType> expected = generateSingleFeature(bridgeImage, oldFeatureId);
		RandomAccessibleInterval<FloatType> result = createStack(bridgeImg, newFeature);
		Utils.assertImagesEqual(expectedPsnr, expected, result);
		List<String> expectedLabels = oldAttributes(oldFeatureId);
		List<String> actualLabels = getAttributeLabels(newFeature);
		assertEquals(expectedLabels, actualLabels);
	}

	private List<String> oldAttributes(int featureConstant) {
		FeatureStack stack = getFeatureStack(bridgeImage, getEnabledFeatures(featureConstant));
		Instances instances = stack.createInstances(new ArrayList<>(Arrays.asList("class1", "class2")));
		return IntStream.range(0, instances.classIndex()).mapToObj(i -> instances.attribute(i).name()).collect(Collectors.toList());
	}

	private List<String> getAttributeLabels(Feature feature) {
		return new FeatureGroup(new IdendityFeature(), feature).attributeLabels();
	}

	@Test
	public void testHessianStack() {
		testFeature(40, FeatureStack.HESSIAN, HessianFeature.group());
	}

	@Test
	public void testDifferenceOfGaussian() {
		testFeature(40, FeatureStack.DOG, DifferenceOfGaussiansFeature.group());
	}

	@Test
	public void testSobel() {
		testFeature(40, FeatureStack.SOBEL, GradientFeature.group());
	}

	@Test
	public void testLipschitz() {
		testFeature(40, FeatureStack.LIPSCHITZ, LipschitzFeature.group(0));
	}

	/** Show that there is a difference between HyperSphereShape and shape used by FilterRank. */
	@Ignore
	@Test
	public void sphereFilterRankHyperSphereEqual() {
		ImagePlus image = squarePictureWithCenteredDot();
		int radius = 30;
		final ImageProcessor ip = image.getProcessor().duplicate();
		final RankFilters filter = new RankFilters();
		filter.rank(ip, radius, RankFilters.MAX);
		Img<FloatType> sphereIj1 = ImageJFunctions.convertFloat(new ImagePlus("ij1_shape", ip));
		Img<FloatType> sphereImgLib = Dilation.dilate(ImageJFunctions.convertFloat(image), new HyperSphereShape(radius), 10);
		Utils.assertImagesEqual(40, sphereIj1, sphereImgLib);
	}

	private static ImagePlus squarePictureWithCenteredDot() {
	    int length = 127;
	    int center = length / 2;
		ByteProcessor processor = new ByteProcessor(length, length);
		processor.set(center, center, 255);
		return new ImagePlus("Square with centered dot", processor);
	}

	@Test
	public void testMaximum() {
		testFeature(27, FeatureStack.MAXIMUM, ShapedFeature.max());
	}

	@Test
	public void testMinimum() {
		testFeature(30, FeatureStack.MINIMUM, ShapedFeature.min());
	}

	@Test
	public void testMean() {
		testFeature(40, FeatureStack.MEAN, ShapedFeature.mean());
	}

	@Test
	public void testVariance() {
		testFeature(30, FeatureStack.VARIANCE, ShapedFeature.variance());
	}

	@Test
	public void testMedian() {
		testFeature(30, FeatureStack.MEDIAN, ShapedFeature.median());
	}

	@Test
	public void testGabor() {
		testFeature(30, FeatureStack.GABOR, GaborFeature.legacy());
	}

	@Test
	public void testNormalize() {
		Img<FloatType> expected = ImageJFunctions.wrapFloat(new ImagePlus("", trainableSegmentation.utils.Utils.normalize(bridgeImage.getStack())));
		Img<FloatType> result = Utils.copy(bridgeImg);
		GaborFeature.normalize(result);
		Utils.assertImagesEqual(expected, result);
	}

	private static RandomAccessibleInterval<FloatType> generateSingleFeature(ImagePlus image, int feature) {
		return generateFeatures(image, getEnabledFeatures(feature));
	}

	private static boolean[] getEnabledFeatures(int feature) {
		boolean[] enabledFeatures = nCopiesAsArray(20, false);
		enabledFeatures[feature] = true;
		return enabledFeatures;
	}

	private static RandomAccessibleInterval<FloatType> generateFeatures(ImagePlus image, boolean[] enabledFeatures) {
		final FeatureStack sliceFeatures = getFeatureStack(image, enabledFeatures);
		ImageStack stack = sliceFeatures.getStack();
		return ImageJFunctions.wrapFloat(new ImagePlus("stack", stack));
	}

	private static FeatureStack getFeatureStack(ImagePlus image, boolean[] enabledFeatures) {
		final FeatureStack sliceFeatures = new FeatureStack(image);
		sliceFeatures.setEnabledFeatures(enabledFeatures);
		sliceFeatures.setMaximumSigma(16);
		sliceFeatures.setMinimumSigma(1);
		sliceFeatures.setMembranePatchSize(19);
		sliceFeatures.setMembraneSize(1);
		sliceFeatures.updateFeaturesST();
		return sliceFeatures;
	}

	public static boolean[] nCopiesAsArray(int count, boolean value) {
		boolean[] array = new boolean[count];
		Arrays.fill(array, value);
		return array;
	}
}