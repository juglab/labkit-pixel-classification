/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.gpu.random_forest;

import hr.irb.fastRandomForest.FastRandomForest;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.random_forest.TransparentRandomForest;
import sc.fiji.labkit.pixel_classification.utils.ArrayUtils;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps around a {@link FastRandomForest}. Allows fast pixel wise application
 * of the random forest on a feature stack.
 * <p>
 * This class achieves better performance than the {@link FastRandomForest} by
 * representing the random forest in a set of flat arrays rather than a object
 * hierarchy.
 * <p>
 * The code can run on CPU or GPU.
 */
public class GpuRandomForestPrediction {

	private final int numberOfClasses;

	private final int numberOfFeatures;

	private final int numberOfTrees;

	private final int numberOfNodes;

	private final int numberOfLeafs;

	private final short[] nodeIndices;

	private final float[] nodeThresholds;

	private final float[] leafProbabilities;

	public GpuRandomForestPrediction(FastRandomForest classifier, int numberOfFeatures) {
		TransparentRandomForest forest =
			TransparentRandomForest.forFastRandomForest(classifier);
		List<GpuRandomTreePrediction> trees = forest.trees().stream().map(
			GpuRandomTreePrediction::new)
			.collect(Collectors.toList());
		this.numberOfClasses = forest.numberOfClasses();
		this.numberOfFeatures = numberOfFeatures;
		this.numberOfTrees = trees.size();
		this.numberOfNodes = trees.stream().mapToInt(x -> x.numberOfNodes).max().orElse(0);
		this.numberOfLeafs = trees.stream().mapToInt(x -> x.numberOfLeafs).max().orElse(0);
		this.nodeIndices = new short[numberOfTrees * numberOfNodes * 3];
		this.nodeThresholds = new float[numberOfTrees * numberOfNodes];
		this.leafProbabilities = new float[numberOfTrees * numberOfLeafs * numberOfClasses];
		for (int j = 0; j < numberOfTrees; j++) {
			GpuRandomTreePrediction tree = trees.get(j);
			for (int i = 0; i < tree.numberOfNodes; i++) {
				nodeIndices[(j * numberOfNodes + i) * 3] = (short) tree.attributeIndicies[i];
				nodeIndices[(j * numberOfNodes + i) * 3 + 1] = (short) tree.smallerChild[i];
				nodeIndices[(j * numberOfNodes + i) * 3 + 2] = (short) tree.biggerChild[i];
				nodeThresholds[j * numberOfNodes + i] = (float) tree.threshold[i];
			}
			for (int i = 0; i < tree.numberOfLeafs; i++)
				for (int k = 0; k < numberOfClasses; k++)
					leafProbabilities[(j * numberOfLeafs + i) * numberOfClasses + k] =
						(float) tree.classProbabilities[i][k];
		}
	}

	public int numberOfClasses() {
		return numberOfClasses;
	}

	public int numberOfFeatures() {
		return numberOfFeatures;
	}

	/**
	 * Applies the random forest to each pixel of the featureStack. Writes the class
	 * probabilities to the output image.
	 * 
	 * @param gpu
	 * @param featureStack Input image. Number of channels must equal
	 *          {@link #numberOfFeatures()}.
	 * @param distribution Output image. Number of channels must equal
	 *          {@link #numberOfClasses()}.
	 */
	public void distribution(GpuApi gpu, GpuImage featureStack, GpuImage distribution) {
		try (GpuApi scope = gpu.subScope()) {
			Img<UnsignedShortType> indices = ArrayImgs.unsignedShorts(nodeIndices, 3, numberOfNodes,
				numberOfTrees);
			Img<FloatType> thresholds = ArrayImgs.floats(nodeThresholds, 1, numberOfNodes, numberOfTrees);
			Img<FloatType> probabilities = ArrayImgs.floats(leafProbabilities, numberOfClasses,
				numberOfLeafs, numberOfTrees);
			GpuImage thresholdsClBuffer = scope.push(thresholds);
			GpuImage probabilitiesClBuffer = scope.push(probabilities);
			GpuImage indicesClBuffer = scope.push(indices);
			GpuRandomForestKernel.randomForest(scope, distribution,
				featureStack,
				thresholdsClBuffer, probabilitiesClBuffer, indicesClBuffer, numberOfFeatures);
		}
	}

	/**
	 * Applies the random forest to each pixel in the feature stack. Write the index
	 * of the class with the highest probability into the output image.
	 * 
	 * @param gpu
	 * @param featureStack Input image. Number of channels must equal
	 *          {@link #numberOfFeatures()}.
	 */
	public GpuImage segment(GpuApi gpu, GpuImage featureStack) {
		try (GpuApi scope = gpu.subScope()) {
			GpuImage distribution = scope.create(featureStack.getDimensions(), numberOfClasses,
				NativeTypeEnum.Float);
			distribution(scope, featureStack, distribution);
			GpuImage output = gpu.create(distribution.getDimensions(), NativeTypeEnum.UnsignedShort);
			GpuRandomForestKernel.findMax(scope, distribution, output);
			return output;
		}
	}

	/**
	 * Applies the random forest to the given instance. Writes the class
	 * probabilities to the parameter called distribution.
	 * 
	 * @param instance Instance / feature vector, must be an array of length
	 *          {@link #numberOfFeatures}.
	 * @param distribution This is the output buffer, array length mush equal
	 *          {@link #numberOfFeatures}.
	 */
	private void distributionForInstance(float[] instance,
		float[] distribution)
	{
		Arrays.fill(distribution, 0);
		for (int tree = 0; tree < numberOfTrees; tree++) {
			addDistributionForTree(instance, tree, distribution);
		}
		ArrayUtils.normalize(distribution);
	}

	private void addDistributionForTree(float[] instance, int tree, float[] distribution) {
		int node = 0;
		while (node >= 0) {
			int nodeOffset = tree * numberOfNodes + node;
			int attributeIndex = nodeIndices[nodeOffset * 3];
			float attributeValue = instance[attributeIndex];
			int b = attributeValue < nodeThresholds[nodeOffset] ? 1 : 2;
			node = nodeIndices[nodeOffset * 3 + b];
		}
		int leaf = node - Short.MIN_VALUE;
		int leafOffset = (tree * numberOfLeafs + leaf) * numberOfClasses;
		for (int k = 0; k < numberOfClasses; k++)
			distribution[k] += leafProbabilities[leafOffset + k];
	}

}
