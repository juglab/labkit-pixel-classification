/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.random_forest;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.utils.ArrayUtils;
import sc.fiji.labkit.pixel_classification.utils.views.FastViews;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.Composite;
import net.imglib2.loops.LoopBuilder;

public class CpuRandomForestPrediction {

	private final CpuRandomForestCore core;

	private final int numberOfFeatures;

	public CpuRandomForestPrediction(FastRandomForest forest, int numberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
		this.core = new CpuRandomForestCore(forest);
	}

	/**
	 * Applies the random forest to each pixel in the feature stack. Write the index
	 * of the class with the highest probability into the output image.
	 *
	 * @param featureStack Input image. Axis order should be XYZC of XYC. Number of
	 *          channels must equal {@link #numberOfFeatures()}.
	 * @param out Output image. Axis order should be XYZ or XY. Pixel values will be
	 *          between 0 and {@link #numberOfClasses()} - 1.
	 */
	public void segment(RandomAccessibleInterval<FloatType> featureStack,
		RandomAccessibleInterval<? extends IntegerType<?>> out)
	{
		LoopBuilder.setImages(FastViews.collapse(featureStack), out).forEachChunk(chunk -> {
			float[] features = new float[numberOfFeatures];
			float[] probabilities = new float[numberOfClasses()];
			chunk.forEachPixel((featureVector, classIndex) -> {
				copyFromTo(featureVector, features);
				core.distributionForInstance(features, probabilities);
				classIndex.setInteger(ArrayUtils.findMax(probabilities));
			});
			return null;
		});
	}

	/**
	 * Applies the random forest for each pixel in the feature stack. Writes the
	 * class probabilities into the output image.
	 *
	 * @param featureStack Image with axis order XYZC or XYC. Where the channel axes
	 *          length equals {@link #numberOfFeatures()}.
	 * @param out Output image axis order must match the input image. Channel axes
	 *          length must equal {@link #numberOfClasses()}.
	 */
	public void distribution(RandomAccessibleInterval<FloatType> featureStack,
		RandomAccessibleInterval<? extends RealType<?>> out)
	{
		LoopBuilder.setImages(FastViews.collapse(featureStack), FastViews.collapse(out)).forEachChunk(
			chunk -> {
				float[] features = new float[numberOfFeatures];
				float[] probabilities = new float[numberOfClasses()];
				chunk.forEachPixel((featureVector, probabilityVector) -> {
					copyFromTo(featureVector, features);
					core.distributionForInstance(features, probabilities);
					copyFromTo(probabilities, probabilityVector);
				});
				return null;
			});
	}

	private static void copyFromTo(Composite<FloatType> input, float[] output) {
		for (int i = 0, len = output.length; i < len; i++)
			output[i] = input.get(i).getRealFloat();
	}

	private static void copyFromTo(float[] input, Composite<? extends RealType<?>> output) {
		for (int i = 0, len = input.length; i < len; i++)
			output.get(i).setReal(input[i]);
	}

	public int numberOfFeatures() {
		return numberOfFeatures;
	}

	public int numberOfClasses() {
		return core.numberOfClasses();
	}
}
