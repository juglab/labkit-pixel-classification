/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.Utils;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;
import trainableSegmentation.FeatureStack3D;
import trainableSegmentation.FeatureStackArray;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class FeatureStack3DTest {

	private Context context = SingletonContext.getInstance();
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

	private void testFeatureIgnoreAttributes(int expectedPsnr, int featureID,
		FeatureSetting featureSetting)
	{
		FeatureStackArray fsa = calculateFeatureStack(featureID);
		FeatureCalculator group = FeatureCalculator.default2d()
			.dimensions(3)
			.sigmaRange(1.0, 8.0)
			.addFeature(SingleFeatures.identity())
			.addFeature(featureSetting)
			.build();
		testFeatures(expectedPsnr, fsa, group);
	}

	private void testFeatures(int expectedPsnr, FeatureStackArray fsa, FeatureCalculator group) {
		RandomAccessibleInterval<FloatType> expected = stripOriginalAndBorder(getImage(fsa));
		RandomAccessibleInterval<FloatType> result = stripOriginalAndBorder(group.apply(img));
		Utils.assertImagesEqual(expectedPsnr, expected, result);
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

	private Img<FloatType> stackToImg(ImageStack stack1, String title) {
		ImagePlus imagePlus = new ImagePlus(title, stack1);
		return ImageJFunctions.wrap(imagePlus);
	}
}
