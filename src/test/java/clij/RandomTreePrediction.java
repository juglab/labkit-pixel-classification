
package clij;

import weka.core.Instance;

public class RandomTreePrediction implements SimpleClassifier {

	final int numberOfNodes;
	final int numberOfLeafs;
	int nodeCount = 0;
	int leafCount = 0;
	final int[] attributeIndicies;
	final double[] threshold;
	final int[] smallerChild;
	final int[] biggerChild;
	final double[][] classProbabilities;

	public RandomTreePrediction(MyRandomTree tree) {
		this.numberOfLeafs = countLeafs(tree);
		this.numberOfNodes = countNodes(tree);
		this.attributeIndicies = new int[numberOfNodes];
		this.threshold = new double[numberOfNodes];
		this.smallerChild = new int[numberOfNodes];
		this.biggerChild = new int[numberOfNodes];
		this.classProbabilities = new double[numberOfLeafs][];
		addTree(tree);
	}

	private int countNodes(MyRandomTree node) {
		return node.isLeaf() ? 0 : 1 + countNodes(node.childLessThan()) + countNodes(node
			.childGreaterThan());
	}

	private int countLeafs(MyRandomTree node) {
		return node.isLeaf() ? 1 : countLeafs(node.childLessThan()) + countLeafs(node
			.childGreaterThan());
	}

	int addTree(MyRandomTree node) {
		return node.isLeaf() ? addLeaf(node) : addNode(node);
	}

	private int addNode(MyRandomTree node) {
		int i = nodeCount++;
		attributeIndicies[i] = node.attributeIndex();
		threshold[i] = node.threshold();
		smallerChild[i] = addTree(node.childLessThan());
		biggerChild[i] = addTree(node.childGreaterThan());
		return i;
	}

	private int addLeaf(MyRandomTree node) {
		int i = leafCount++;
		if (i >= classProbabilities.length)
			throw new AssertionError();
		classProbabilities[i] = node.classProbabilities();
		return -1 - i;
	}

	@Override
	public double[] distributionForInstance(Instance instance) {
		int nodeIndex = 0;
		while (nodeIndex >= 0) {
			int attributeIndex = attributeIndicies[nodeIndex];
			double attributeValue = instance.value(attributeIndex);
			nodeIndex = (attributeValue < threshold[nodeIndex]) ? smallerChild[nodeIndex]
				: biggerChild[nodeIndex];
		}
		int leafIndex = -1 - nodeIndex;
		return classProbabilities[leafIndex];
	}
}
