/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.pixel_feature.filter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.dog.DifferenceOfGaussiansFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.gauss.GaussFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.gradient.SobelGradientFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.hessian.HessianFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.stats.SingleSphereShapedFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.stats.SphereShapedFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Ignore;
import org.junit.Test;
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

	private final ImagePlus bridgeImage = Utils.loadImage("nuclei.tif");

	private final Img<FloatType> bridgeImg = ImagePlusAdapter.convertFloat(bridgeImage);

	public static RandomAccessibleInterval<FloatType> createStack(
		RandomAccessibleInterval<FloatType> image, FeatureSetting feature)
	{
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.sigmaRange(1.0, 16.0)
			.addFeature(SingleFeatures.identity())
			.addFeature(feature)
			.build();
		return calculator.apply(image);
	}

	@Test
	public void testEmptyStack() {
		RandomAccessibleInterval<FloatType> expected = generateFeatures(bridgeImage, nCopiesAsArray(20,
			false));
		RandomAccessibleInterval<FloatType> result = createEmptyStack(bridgeImg);
		Utils.assertImagesEqual(expected, result);
	}

	private RandomAccessibleInterval<FloatType> createEmptyStack(Img<FloatType> image) {
		return image;
	}

	@Deprecated
	@Test
	public void testGaussStack() {
		testFeatureIgnoreAttributes(40, FeatureStack.GAUSSIAN, new FeatureSetting(GaussFeature.class));
	}

	private void testFeature(float expectedPsnr, int oldFeatureId, FeatureSetting newFeature) {
		testFeatureIgnoreAttributes(expectedPsnr, oldFeatureId, newFeature);
		testAttributes(oldFeatureId, newFeature);
	}

	private void testFeatureIgnoreAttributes(float expectedPsnr, int oldFeatureId,
		FeatureSetting newFeature)
	{
		RandomAccessibleInterval<FloatType> expected = generateSingleFeature(bridgeImage, oldFeatureId);
		RandomAccessibleInterval<FloatType> result = createStack(bridgeImg, newFeature);
		Utils.assertImagesEqual(expectedPsnr, expected, result);
	}

	private void testAttributes(int oldFeatureId, FeatureSetting newFeature) {
		List<String> expectedLabels = oldAttributes(oldFeatureId);
		List<String> actualLabels = getAttributeLabels(newFeature);
		assertEquals(expectedLabels, actualLabels);
	}

	private List<String> oldAttributes(int featureConstant) {
		return oldAttributes(getFeatureStack(bridgeImage, getEnabledFeatures(featureConstant)));
	}

	static List<String> oldAttributes(FeatureStack stack) {
		Instances instances = stack.createInstances(new ArrayList<>(Arrays.asList("class1", "class2")));
		return IntStream.range(0, instances.classIndex()).mapToObj(i -> instances.attribute(i).name())
			.collect(Collectors.toList());
	}

	private static List<String> getAttributeLabels(FeatureSetting feature) {
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.sigmaRange(1.0, 16.0)
			.addFeature(SingleFeatures.identity())
			.addFeature(feature)
			.build();
		return calculator.attributeLabels();
	}

	@Deprecated
	@Test
	public void testHessianStack() {
		testFeature(40, FeatureStack.HESSIAN, new FeatureSetting(HessianFeature.class));
	}

	@Deprecated
	@Test
	public void testDifferenceOfGaussian() {
		testFeature(40, FeatureStack.DOG, new FeatureSetting(DifferenceOfGaussiansFeature.class));
	}

	@Deprecated
	@Test
	public void testSobel() {
		testFeature(40, FeatureStack.SOBEL, new FeatureSetting(SobelGradientFeature.class));
	}

	@Deprecated
	@Test
	public void testLipschitz() {
		testFeature(40, FeatureStack.LIPSCHITZ, GroupedFeatures.lipschitz(0));
	}

	/**
	 * Show that there is a difference between HyperSphereShape and shape used by
	 * FilterRank.
	 */
	@Ignore
	@Test
	public void sphereFilterRankHyperSphereEqual() {
		ImagePlus image = squarePictureWithCenteredDot();
		int radius = 30;
		final ImageProcessor ip = image.getProcessor().duplicate();
		final RankFilters filter = new RankFilters();
		filter.rank(ip, radius, RankFilters.MAX);
		Img<FloatType> sphereIj1 = ImageJFunctions.convertFloat(new ImagePlus("ij1_shape", ip));
		Img<FloatType> sphereImgLib = Dilation.dilate(ImageJFunctions.convertFloat(image),
			new HyperSphereShape(radius), 10);
		Utils.assertImagesEqual(40, sphereIj1, sphereImgLib);
	}

	private static ImagePlus squarePictureWithCenteredDot() {
		int length = 127;
		int center = length / 2;
		ByteProcessor processor = new ByteProcessor(length, length);
		processor.set(center, center, 255);
		return new ImagePlus("Square with centered dot", processor);
	}

	@Deprecated
	@Test
	public void testMaximum() {
		testFeature(27, FeatureStack.MAXIMUM, new FeatureSetting(SphereShapedFeature.class, "operation",
			SingleSphereShapedFeature.MAX));
	}

	@Deprecated
	@Test
	public void testMinimum() {
		testFeature(30, FeatureStack.MINIMUM, new FeatureSetting(SphereShapedFeature.class, "operation",
			SingleSphereShapedFeature.MIN));
	}

	@Deprecated
	@Test
	public void testMean() {
		testFeature(40, FeatureStack.MEAN, new FeatureSetting(SphereShapedFeature.class, "operation",
			SingleSphereShapedFeature.MEAN));
	}

	@Deprecated
	@Test
	public void testVariance() {
		testFeature(30, FeatureStack.VARIANCE, new FeatureSetting(SphereShapedFeature.class,
			"operation",
			SingleSphereShapedFeature.VARIANCE));
	}

	@Deprecated
	@Test
	public void testMedian() {
		testFeature(30, FeatureStack.MEDIAN, new FeatureSetting(SphereShapedFeature.class, "operation",
			SingleSphereShapedFeature.MEDIAN));
	}

	@Deprecated
	@Test
	public void testGabor() {
		testFeature(30, FeatureStack.GABOR, GroupedFeatures.legacyGabor());
	}

	private static RandomAccessibleInterval<FloatType> generateSingleFeature(ImagePlus image,
		int feature)
	{
		return generateFeatures(image, getEnabledFeatures(feature));
	}

	private static boolean[] getEnabledFeatures(int feature) {
		boolean[] enabledFeatures = nCopiesAsArray(20, false);
		enabledFeatures[feature] = true;
		return enabledFeatures;
	}

	private static RandomAccessibleInterval<FloatType> generateFeatures(ImagePlus image,
		boolean[] enabledFeatures)
	{
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
