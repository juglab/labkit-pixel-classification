
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import weka.core.Instance;

import java.util.List;
import java.util.stream.Collectors;

public class RandomForestPrediction implements SimpleClassifier {

	private final int numberOfClasses;

	private final int numberOfTrees;

	private final int numberOfNodes;

	private final int numberOfLeafs;

	private final short[] nodeIndices;

	private final float[] nodeThresholds;

	private final float[] leafProbabilities;

	public RandomForestPrediction(MyRandomForest forest, int numberOfClasses) {
		List<RandomTreePrediction> trees = forest.trees().stream().map(RandomTreePrediction::new)
			.collect(Collectors.toList());
		this.numberOfClasses = numberOfClasses;
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
				nodeIndices[(j * numberOfNodes + i) * 3 + 1] = encodeLeaf(tree.smallerChild[i]);
				nodeIndices[(j * numberOfNodes + i) * 3 + 2] = encodeLeaf(tree.biggerChild[i]);
				nodeThresholds[j * numberOfNodes + i] = (float) tree.threshold[i];
			}
			for (int i = 0; i < tree.leafCount; i++)
				for (int k = 0; k < numberOfClasses; k++)
					leafProbabilities[(j * numberOfLeafs + i) * numberOfClasses + k] =
						(float) tree.classProbabilities[i][k];
		}
	}

	private short encodeLeaf(int index) {
		return (short) (index >= 0 ? index : numberOfNodes - 1 - index);
	}

	@Override
	public double[] distributionForInstance(Instance instance) {
		double[] distribution = new double[numberOfClasses];
		for (int tree = 0; tree < numberOfTrees; tree++) {
			addDistributionForTree(instance, tree, distribution);
		}
		return ArrayUtils.normalize(distribution);
	}

	private void addDistributionForTree(Instance instance, int tree, double[] distribution) {
		int node = 0;
		while (node >= numberOfNodes) {
			int attributeIndex = nodeIndices[(tree * numberOfNodes + node) * 3];
			double attributeValue = instance.value(attributeIndex);
			int b = attributeValue < nodeThresholds[(tree * numberOfNodes) + node] ? 1 : 2;
			node = nodeIndices[(tree * numberOfNodes + node) * 3 + b];
		}
		int leaf = node - numberOfNodes;
		for (int k = 0; k < numberOfClasses; k++)
			distribution[k] += leafProbabilities[(tree * numberOfLeafs + leaf) * numberOfClasses + k];
	}

	public void distribution(CLIJ2 clij, ClearCLBuffer features, ClearCLBuffer distribution) {
		long numberOfSlices = distribution.getDepth() / numberOfClasses;
		int numberOfFeatures = (int) (features.getDepth() / numberOfSlices);
		Img<UnsignedShortType> indices = ArrayImgs.unsignedShorts(nodeIndices, 3, numberOfNodes,
			numberOfTrees);
		Img<FloatType> thresholds = ArrayImgs.floats(nodeThresholds, 1, numberOfNodes, numberOfTrees);
		Img<FloatType> probabilities = ArrayImgs.floats(leafProbabilities, numberOfClasses,
			numberOfLeafs, numberOfTrees);
		ClijRandomForestKernel.randomForest(clij,
			distribution,
			features,
			clij.push(thresholds),
			clij.push(probabilities),
			clij.push(indices),
			numberOfFeatures);
	}
}
