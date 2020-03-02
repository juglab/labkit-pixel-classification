
package net.imglib2.trainable_segmention.clij_random_forest;

import weka.core.Instance;

public class RandomTreePrediction {

	final int numberOfNodes;
	final int numberOfLeafs;
	int nodeCount = 0;
	int leafCount = 0;
	final int[] attributeIndicies;
	final double[] threshold;
	final int[] smallerChild;
	final int[] biggerChild;
	final double[][] classProbabilities;

	public RandomTreePrediction(TransparentRandomTree tree) {
		this.numberOfLeafs = countLeafs(tree);
		this.numberOfNodes = countNodes(tree);
		this.attributeIndicies = new int[numberOfNodes];
		this.threshold = new double[numberOfNodes];
		this.smallerChild = new int[numberOfNodes];
		this.biggerChild = new int[numberOfNodes];
		this.classProbabilities = new double[numberOfLeafs][];
		addTree(tree);
	}

	private int countNodes(TransparentRandomTree node) {
		return node.isLeaf() ? 0 : 1 + countNodes(node.smallerChild()) + countNodes(node
			.biggerChild());
	}

	private int countLeafs(TransparentRandomTree node) {
		return node.isLeaf() ? 1 : countLeafs(node.smallerChild()) + countLeafs(node
			.biggerChild());
	}

	int addTree(TransparentRandomTree node) {
		return node.isLeaf() ? addLeaf(node) : addNode(node);
	}

	private int addNode(TransparentRandomTree node) {
		int i = nodeCount++;
		attributeIndicies[i] = node.attributeIndex();
		threshold[i] = node.threshold();
		smallerChild[i] = addTree(node.smallerChild());
		biggerChild[i] = addTree(node.biggerChild());
		return i;
	}

	private int addLeaf(TransparentRandomTree node) {
		int i = leafCount++;
		if (i >= classProbabilities.length)
			throw new AssertionError();
		classProbabilities[i] = node.classProbabilities();
		return -1 - i;
	}

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
