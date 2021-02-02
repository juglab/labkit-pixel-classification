
package net.imglib2.trainable_segmentation.gpu.random_forest;

import hr.irb.fastRandomForest.FastRandomForest;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmentation.gpu.api.GpuApi;
import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import preview.net.imglib2.loops.LoopUtils;
import preview.net.imglib2.loops.SyncedPositionables;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class calculates the same prediction as {@link FastRandomForest}. But it
 * uses flat arrays rather than a tree data structure to represent the tree in
 * memory.
 * <p>
 * This allows the algorithm to be executed on GPU via CLIJ.
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

	public RandomForestPrediction(FastRandomForest classifier, int numberOfClasses,
		int numberOfFeatures)
	{
		TransparentRandomForest forest = new TransparentRandomForest(classifier);
		List<RandomTreePrediction> trees = forest.trees().stream().map(RandomTreePrediction::new)
			.collect(Collectors.toList());
		this.numberOfClasses = numberOfClasses;
		this.numberOfFeatures = numberOfFeatures;
		this.numberOfTrees = trees.size();
		this.numberOfNodes = trees.stream().mapToInt(x -> x.numberOfNodes).max().getAsInt();
		this.numberOfLeafs = trees.stream().mapToInt(x -> x.numberOfLeafs).max().getAsInt();
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

	public void distribution(GpuApi gpu, GpuImage features, GpuImage distribution) {
		try (GpuApi scope = gpu.subScope()) {
			Img<UnsignedShortType> indices = ArrayImgs.unsignedShorts(nodeIndices, 3, numberOfNodes,
				numberOfTrees);
			Img<FloatType> thresholds = ArrayImgs.floats(nodeThresholds, 1, numberOfNodes, numberOfTrees);
			Img<FloatType> probabilities = ArrayImgs.floats(leafProbabilities, numberOfClasses,
				numberOfLeafs, numberOfTrees);
			GpuImage thresholdsClBuffer = scope.push(thresholds);
			GpuImage probabilitiesClBuffer = scope.push(probabilities);
			GpuImage indicesClBuffer = scope.push(indices);
			GpuRandomForestKernel.randomForest(scope, distribution, features,
				thresholdsClBuffer, probabilitiesClBuffer, indicesClBuffer, numberOfFeatures);
		}
	}

	public GpuImage segment(GpuApi gpu, GpuImage features) {
		try (GpuApi scope = gpu.subScope()) {
			GpuImage distribution = scope.create(features.getDimensions(), numberOfClasses,
				NativeTypeEnum.Float);
			distribution(scope, features, distribution);
			GpuImage output = gpu.create(distribution.getDimensions(), NativeTypeEnum.UnsignedShort);
			GpuRandomForestKernel.findMax(scope, distribution, output);
			return output;
		}
	}

	public void segment(RandomAccessibleInterval<FloatType> features,
		RandomAccessibleInterval<? extends IntegerType<?>> out)
	{
		RandomAccess<FloatType> ra = features.randomAccess();
		ra.setPosition(Intervals.minAsLongArray(features));
		RandomAccess<? extends IntegerType<?>> o = out.randomAccess();
		o.setPosition(Intervals.minAsLongArray(out));
		int d = features.numDimensions() - 1;
		float[] attr = new float[numberOfFeatures];
		float[] distribution = new float[numberOfClasses];
		LoopUtils.createIntervalLoop(SyncedPositionables.create(ra, o), out, () -> {
			for (int i = 0; i < attr.length; i++) {
				ra.setPosition(i, d);
				attr[i] = ra.get().getRealFloat();
			}
			distributionForInstance(attr, distribution);
			o.get().setInteger(ArrayUtils.findMax(distribution));
		}).run();
	}

	private void distributionForInstance(float[] attr,
		float[] distribution)
	{
		Arrays.fill(distribution, 0);
		for (int tree = 0; tree < numberOfTrees; tree++) {
			addDistributionForTree(attr, tree, distribution);
		}
		ArrayUtils.normalize(distribution);
	}

	private void addDistributionForTree(float[] attr, int tree, float[] distribution) {
		int node = 0;
		while (node >= 0) {
			int nodeOffset = tree * numberOfNodes + node;
			int attributeIndex = nodeIndices[nodeOffset * 3];
			float attributeValue = attr[attributeIndex];
			int b = attributeValue < nodeThresholds[nodeOffset] ? 1 : 2;
			node = nodeIndices[nodeOffset * 3 + b];
		}
		int leaf = node - Short.MIN_VALUE;
		int leafOffset = (tree * numberOfLeafs + leaf) * numberOfClasses;
		for (int k = 0; k < numberOfClasses; k++)
			distribution[k] += leafProbabilities[leafOffset + k];
	}

	public void distribution(RandomAccessibleInterval<FloatType> features,
		RandomAccessibleInterval<? extends RealType<?>> out)
	{
		RandomAccess<FloatType> ra = features.randomAccess();
		ra.setPosition(Intervals.minAsLongArray(features));
		RandomAccess<? extends RealType<?>> o = out.randomAccess();
		o.setPosition(Intervals.minAsLongArray(out));
		int d = features.numDimensions() - 1;
		int d_out = out.numDimensions() - 1;
		float[] attr = new float[numberOfFeatures];
		float[] distribution = new float[numberOfClasses];
		Interval interval = Intervals.hyperSlice(out, d_out);
		LoopUtils.createIntervalLoop(SyncedPositionables.create(ra, o), interval, () -> {
			for (int i = 0; i < attr.length; i++) {
				ra.setPosition(i, d);
				attr[i] = ra.get().getRealFloat();
			}
			distributionForInstance(attr, distribution);
			for (int i = 0; i < distribution.length; i++) {
				o.setPosition(i, d_out);
				o.get().setReal(distribution[i]);
			}
		}).run();

	}
}
