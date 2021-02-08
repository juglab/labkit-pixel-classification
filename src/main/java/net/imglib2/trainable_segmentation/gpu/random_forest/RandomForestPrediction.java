
package net.imglib2.trainable_segmentation.gpu.random_forest;

import hr.irb.fastRandomForest.FastRandomForest;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmentation.gpu.api.GpuApi;
import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.imglib2.trainable_segmentation.utils.views.FastViews;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.StopWatch;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import preview.net.imglib2.loops.LoopBuilder;

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
public class RandomForestPrediction {

	private final int numberOfClasses;

	private final int numberOfFeatures;

	private final int numberOfTrees;

	private final int numberOfNodes;

	private final int numberOfLeafs;

	private final short[] nodeIndices;

	private final float[] nodeThresholds;

	private final float[] leafProbabilities;

	public RandomForestPrediction(FastRandomForest classifier, int numberOfFeatures) {
		TransparentRandomForest forest = new TransparentRandomForest(classifier);
		List<RandomTreePrediction> trees = forest.trees().stream().map(RandomTreePrediction::new)
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
			RandomTreePrediction tree = trees.get(j);
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
		StopWatch watch = StopWatch.createAndStart();
		LoopBuilder.setImages(FastViews.collapse(featureStack), out).forEachChunk(chunk -> {
			float[] features = new float[numberOfFeatures];
			float[] probabilities = new float[numberOfClasses];
			chunk.forEachPixel((featureVector, classIndex) -> {
				copyFromTo(featureVector, features);
				distributionForInstance(features, probabilities);
				classIndex.setInteger(ArrayUtils.findMax(probabilities));
			});
			return null;
		});
		System.out.println("segment runtime " + watch);
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
		LoopBuilder.setImages(FastViews.collapse(featureStack), FastViews.collapse(out)).forEachChunk(chunk -> {
			float[] features = new float[numberOfFeatures];
			float[] probabilities = new float[numberOfClasses];
			chunk.forEachPixel((featureVector, probabilityVector) -> {
				copyFromTo(featureVector, features);
				distributionForInstance(features, probabilities);
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
