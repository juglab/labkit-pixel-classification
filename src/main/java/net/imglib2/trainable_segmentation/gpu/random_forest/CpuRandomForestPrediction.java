package net.imglib2.trainable_segmentation.gpu.random_forest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.irb.fastRandomForest.FastRandomForest;

class CpuRandomForestPrediction {

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	private static final int COMPACT_STORAGE_MIN_HEIGHT = 4;

	/*
	The random forest is encoded in the following attributes[], thresholds[],
	and probabilities[] arrays.

	Trees are ordered by height. So first all trees of height 1 are put into the
	arrays, then height 2, etc. There are two modes of encoding trees:
	"expanded" and "compact". The COMPRESSED_STORAGE_MIN_HEIGHT constant defines
	at which tree height to switch over to the "compact" storage scheme.

	Expanded Storage:
	-----------------

	Trees are "flattened out" to full binary trees of their respective height.
	For a tree of height h, space for 2^h-1 internal nodes is allocated in
	attributes[] and thresholds[], and space for 2^h leaf nodes is allocated in
	probabilities.

	For an internal tree node i, attributes[i] and thresholds[i] are the index
	of the attribute to select and the threshold to compare it with. If the
	attribute is smaller than the the threshold go to the left child, otherwise
	go to the right child. The left child of node i is at 2*i+1. The right child
	of node i is at 2*i+2.

	At the leafs at maximum depth of the tree, no attributes[] and thresholds[]
	space is required, instead probabilities[] contains the leaf probabilities.

	A value attribute[i] < 0 signifies that this branch of the tree terminates
	at node i (early, i is not at full depth). In this case, the leaf
	probabilities are stored at the position we would reach by always going to
	the left child until we reach full depth.

	Trees are ordered by height. So first all trees of height 1 are put into the
	arrays, then height 2, etc.

	For the first few heights there are special case implementations for
	evaluating the tree. For example, for a tree of height 1 we know that it
	always has exactly one attribute comparison that directly results in either
	of two leaf probability slots.

	Also for accumulating leaf probabilities there are special case
	implementations for 2 classes and 3 classes.

	See generic_distributionForInstance() for the "simple" version without
	special cases.

	Compact Storage:
	-----------------

	For the "compact" scheme, the attributes[] array stores also node indices.
	Only internal tree nodes are stored. Each node takes up 3 attributes[]
	slots. For node i, attributes[3*i] and thresholds[i] are the index of the
	attribute to select and the threshold to compare it with. If the attribute
	is smaller than the the threshold go to the left child, otherwise go to the
	right child. The index of the left child node is stored at
	attributes[3*i+1], the index of the right child node is stored at
	attributes[3*i+2].

	If the left (or right) child is a leaf, instead of a node index,
	attributes[3*i+1] (or attributes[3*i+2]) contains the starting index of the
	leaf respective probabilities. To distinguish the leaf case, starting indices
	are stored as negative numbers (i+Short.MIN_VALUE

	Additionally, before the attributes[] entries of each tree there are two
	entries containing the number of nodes and number of leafs probability slots
	for the tree.

	For accumulating leaf probabilities there are again special case
	implementations for 2 classes and 3 classes.
	*/

	private final short[] attributes;
	private final float[] thresholds;
	private final float[] probabilities;

	private final int numClasses;

	/**
	 * The value at index i is the number of trees of height i+1.
	 * (The length of the array is the maximum height of any tree in the forest.)
	 */
	private final int[] numTreesOfHeight;

	private final float[] prior;

	public CpuRandomForestPrediction(final FastRandomForest classifier,
		final int numberOfFeatures)
	{
		this(TransparentRandomForest.forFastRandomForest(classifier), numberOfFeatures);
	}

	public CpuRandomForestPrediction(final TransparentRandomForest forest,
		final int numberOfFeatures)
	{
		numClasses = forest.numberOfClasses();
		prior = new float[numClasses];

		final Map<Integer, List<TransparentRandomTree>> treesByHeight =
			new HashMap<>();
		for (final TransparentRandomTree tree : forest.trees())
			treesByHeight.computeIfAbsent(tree.height(), ArrayList::new)
				.add(tree);

		// handle trees of height 0 (leaf-only)
		List< TransparentRandomTree > zeroHeightTrees = treesByHeight.getOrDefault(0, Collections.emptyList());
		treesByHeight.remove(0);
		for (TransparentRandomTree leaf : zeroHeightTrees) {
			for (int i = 0; i < numClasses; ++i)
				prior[i] += (float) leaf.classProbabilities()[i];
		}

		final int[] heights =
				treesByHeight.keySet().stream().mapToInt(Integer::intValue).sorted()
						.toArray();

		final int maxHeight =
			heights.length == 0 ? 0 : heights[heights.length - 1];
		numTreesOfHeight = new int[maxHeight];
		long attributesSize = 0;
		long thresholdsSize = 0;
		long probabilitiesSize = 0;
		for (int i = 0; i < numTreesOfHeight.length; ++i) {
			final List<TransparentRandomTree> trees =
				treesByHeight.getOrDefault(i + 1, Collections.emptyList());
			numTreesOfHeight[i] = trees.size();
			final int height = i + 1;
			if (height < COMPACT_STORAGE_MIN_HEIGHT) {
				final long numTrees = trees.size();
				final int numLeafs = 1 << height;
				final int numNonLeafs = numLeafs - 1;
				attributesSize += numNonLeafs * numTrees;
				thresholdsSize += numNonLeafs * numTrees;
				probabilitiesSize += numLeafs * numClasses * numTrees;
			}
			else {
				for (final TransparentRandomTree tree : trees) {
					final int numNodes = tree.numberOfNodes();
					final int numLeafs = tree.numberOfLeafs();
					final int numNonLeafs = numNodes - numLeafs;
					attributesSize += 2 + 3 * numNonLeafs;
					thresholdsSize += numNonLeafs;
					probabilitiesSize += numLeafs * numClasses;
				}
			}
		}

		if (attributesSize > MAX_ARRAY_SIZE ||
			probabilitiesSize > MAX_ARRAY_SIZE)
			throw new IllegalArgumentException(
				"forest is too big to represent in " +
					CpuRandomForestPrediction.class.getSimpleName());

		attributes = new short[(int) attributesSize];
		thresholds = new float[(int) thresholdsSize];
		probabilities = new float[(int) probabilitiesSize];

		int attributesBase = 0;
		int thresholdsBase = 0;
		int probabilitiesBase = 0;
		final int[] j = new int[1];
		for (final int height : heights) {
			final List<TransparentRandomTree> trees = treesByHeight.get(height);
			if (height < COMPACT_STORAGE_MIN_HEIGHT) {
				final int depth = height - 1;
				final int numLeafs = 2 << depth;
				final int numNonLeafs = numLeafs - 1;
				final int dataSize = numNonLeafs;
				final int probSize = numLeafs * numClasses;
				for (final TransparentRandomTree tree : trees) {
					write(tree, 0, 0, 0, height - 1, attributesBase,
						probabilitiesBase);
					attributesBase += dataSize;
					thresholdsBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else {
				for (final TransparentRandomTree tree : trees) {
					j[0] = 0;
					final int size =
						write_compact(tree, 0, j, attributesBase + 2,
							thresholdsBase, probabilitiesBase);
					attributes[attributesBase] = (short) size;
					attributes[attributesBase + 1] = (short) j[0];
					attributesBase += 2 + size * 3;
					thresholdsBase += size;
					probabilitiesBase += j[0];
				}
			}
		}
	}

	/**
	 * Serialize a node into the attributes, thresholds, and probabilities
	 * arrays. This implements the "Expanded Storage" scheme.
	 *
	 * @param node
	 * 	a tree node.
	 * @param nodeIndex
	 * 	flattened index of {@code node}. (Children of node i are at 2*i+1 and
	 * 	2*i+2).
	 * @param branchBits
	 * 	path through the tree to this node. (The {@depth} least significant
	 * 	bits are the branches, e.g., "010" says: from the root go left, then
	 * 	right, then left.)
	 * @param depth
	 * 	depth of {@code node}.
	 * @param maxDepth
	 * 	maximum depth of any node in the current tree (the tree that
	 *    {@code node} belongs to).
	 * @param treeDataBase
	 * 	offset into attributes[], thresholds[] where the current tree is
	 * 	placed.
	 * @param treeProbBase
	 * 	offset into probabilities[] where the current tree is placed.
	 */
	private void write(final TransparentRandomTree node, final int nodeIndex,
		final int branchBits, final int depth, final int maxDepth,
		final int treeDataBase, final int treeProbBase)
	{
		if (node.isLeaf()) {
			final int b;
			if (depth <= maxDepth) {
				// mark as leaf by setting attribute index to -1
				final int o = treeDataBase + nodeIndex;
				attributes[o] = -1;
				b = branchBits << (1 + maxDepth - depth);
			}
			else b = branchBits;
			final int o = treeProbBase + b * numClasses;
			for (int i = 0; i < numClasses; ++i)
				probabilities[o + i] = (float) node.classProbabilities()[i];
		}
		else // not a leaf
		{
			// write feature index and threshold
			final int o = treeDataBase + nodeIndex;
			attributes[o] = (short) node.attributeIndex();
			thresholds[o] = (float) node.threshold();

			// recursively write children
			write(node.smallerChild(), 2 * nodeIndex + 1, (branchBits << 1),
				depth + 1, maxDepth, treeDataBase, treeProbBase);
			write(node.biggerChild(), 2 * nodeIndex + 2, (branchBits << 1) + 1,
				depth + 1, maxDepth, treeDataBase, treeProbBase);
		}
	}

	/**
	 * Serialize a node in into the attributes, thresholds, and probabilities
	 * arrays. This implements the "Compact Storage" scheme.
	 *
	 * @param node
	 * 	a tree node.
	 * @param i
	 * 	index of {@code node}.
	 * @param j
	 * 	j[0] index of next free leaf probabilities slot (will be incremented by
	 * 	this method if leaf probabilities are stored.)
	 * @param attributesBase
	 * 	offset into attributes[] where the current tree is placed.
	 * @param thresholdsBase
	 * 	offset into thresholds[] where the current tree is placed.
	 * @param probabilitiesBase
	 * 	offset into probabilities[] where the current tree is placed.
	 */
	private int write_compact(final TransparentRandomTree node, final int i,
		// index in data
		final int[] j, // index in probabilities
		final int attributesBase, final int thresholdsBase,
		final int probabilitiesBase)
	{
		// write feature index and threshold
		attributes[attributesBase + 3 * i] = (short) node.attributeIndex();
		thresholds[thresholdsBase + i] = (float) node.threshold();

		final int lsize;
		final TransparentRandomTree smaller = node.smallerChild();
		if (smaller.isLeaf()) {
			attributes[attributesBase + 3 * i + 1] =
				(short) (j[0] + Short.MIN_VALUE);
			for (int c = 0; c < numClasses; ++c)
				probabilities[probabilitiesBase + j[0]++] =
					(float) smaller.classProbabilities()[c];
			lsize = 0;
		}
		else {
			attributes[attributesBase + 3 * i + 1] = (short) (i + 1);
			lsize =
				write_compact(smaller, i + 1, j, attributesBase, thresholdsBase,
					probabilitiesBase);
		}

		final int rsize;
		final TransparentRandomTree bigger = node.biggerChild();
		if (bigger.isLeaf()) {
			attributes[attributesBase + 3 * i + 2] =
				(short) (j[0] + Short.MIN_VALUE);
			for (int c = 0; c < numClasses; ++c)
				probabilities[probabilitiesBase + j[0]++] =
					(float) bigger.classProbabilities()[c];
			rsize = 0;
		}
		else {
			attributes[attributesBase + 3 * i + 2] = (short) (i + lsize + 1);
			rsize = write_compact(bigger, i + lsize + 1, j, attributesBase,
				thresholdsBase, probabilitiesBase);
		}

		return 1 + lsize + rsize;
	}

	/**
	 * Applies the random forest to the given instance. Writes the class
	 * probabilities to {@code distribution}. Depending on the number of
	 * classes, this calls {@link #distributionForInstance_c2} (for 2 classes),
	 * {@link #distributionForInstance_c3} (for 3 classes), or
	 * {@link #distributionForInstance_ck} (for {@code >3} classes).
	 *
	 * @param instance
	 * 	Instance / feature vector, array length must equal
	 *    {@code numberOfFeatures}.
	 * @param distribution
	 * 	This is the output buffer, array length must equal number of classes.
	 */
	void distributionForInstance(final float[] instance,
		final float[] distribution)
	{
		switch (numClasses) {
			case 2:
				distributionForInstance_c2(instance, distribution);
				break;
			case 3:
				distributionForInstance_c3(instance, distribution);
				break;
			default:
				distributionForInstance_ck(instance, distribution);
				break;
		}
	}

	/**
	 * Applies the random forest to the given instance.
	 * This implements the general case for arbitrary number of classes.
	 */
	private void distributionForInstance_ck(final float[] instance,
		final float[] distribution)
	{
		final int numClasses = this.numClasses;
		for (int i = 0; i < numClasses; ++i)
			distribution[i] = prior[i];
		int attributesBase = 0;
		int probabilitiesBase = 0;
		int depth = 0;
		for (; depth < numTreesOfHeight.length &&
			depth < COMPACT_STORAGE_MIN_HEIGHT - 1; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			if (depth == 0) // special case for trees of height 1
			{
				final int dataSize = 1;
				final int probSize = 2 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h1(instance, attributesBase);
					acc(distribution, numClasses, probabilitiesBase,
						branchBits * numClasses);
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else if (depth == 1) // special case for trees of height 2
			{
				final int dataSize = 3;
				final int probSize = 4 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h2(instance, attributesBase);
					acc(distribution, numClasses, probabilitiesBase,
						branchBits * numClasses);
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else if (depth == 2) // special case for trees of height 3
			{
				final int dataSize = 7;
				final int probSize = 8 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h3(instance, attributesBase);
					acc(distribution, numClasses, probabilitiesBase,
						branchBits * numClasses);
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else // general case
			{
				final int numLeafs = 2 << depth;
				final int dataSize = numLeafs - 1;
				final int probSize = numLeafs * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree(instance, attributesBase, depth);
					acc(distribution, numClasses, probabilitiesBase,
						branchBits * numClasses);
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
		}

		int thresholdsBase = attributesBase;
		for (; depth < numTreesOfHeight.length; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			for (int tree = 0; tree < nh; ++tree) {
				final int attrSize = attributes[attributesBase];
				final int probSize = attributes[attributesBase + 1];
				int node = 0;
				while (node >= 0) {
					final int attributeIndex =
						attributes[attributesBase + 2 + 3 * node];
					final float attributeValue = instance[attributeIndex];
					final float threshold = thresholds[thresholdsBase + node];
					node = (attributeValue < threshold) ?
						attributes[attributesBase + 2 + 3 * node + 1] :
						attributes[attributesBase + 2 + 3 * node + 2];
				}
				final int j = node - Short.MIN_VALUE;
				acc(distribution, numClasses, probabilitiesBase, j);
				attributesBase += 2 + 3 * attrSize;
				thresholdsBase += attrSize;
				probabilitiesBase += probSize;
			}
		}

		ArrayUtils.normalize(distribution);
	}

	private void acc(final float[] distribution, final int numClasses,
		final int probBase, final int offset)
	{
		for (int k = 0; k < numClasses; k++)
			distribution[k] += probabilities[probBase + offset + k];
	}

	/**
	 * Applies the random forest to the given instance.
	 * This implements the general case for arbitrary number of classes.
	 */
	void distributionForInstance_c2(final float[] instance,
		final float[] distribution)
	{
		float c0 = prior[ 0 ], c1 = prior[ 1 ];
		int attributesBase = 0;
		int probabilitiesBase = 0;
		int depth = 0;
		for (; depth < numTreesOfHeight.length &&
			depth < COMPACT_STORAGE_MIN_HEIGHT - 1; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			if (depth == 0) // special case for trees of height 1
			{
				final int dataSize = 1;
				final int probSize = 2 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h1(instance, attributesBase);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else if (depth == 1) // special case for trees of height 2
			{
				final int dataSize = 3;
				final int probSize = 4 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h2(instance, attributesBase);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else if (depth == 2) // special case for trees of height 3
			{
				final int dataSize = 7;
				final int probSize = 8 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h3(instance, attributesBase);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else // general case
			{
				final int numLeafs = 2 << depth;
				final int dataSize = numLeafs - 1;
				final int probSize = numLeafs * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree(instance, attributesBase, depth);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
		}

		int thresholdsBase = attributesBase;
		for (; depth < numTreesOfHeight.length; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			for (int tree = 0; tree < nh; ++tree) {
				final int attrSize = attributes[attributesBase];
				final int probSize = attributes[attributesBase + 1];
				int node = 0;
				while (node >= 0) {
					final int attributeIndex =
						attributes[attributesBase + 2 + 3 * node];
					final float attributeValue = instance[attributeIndex];
					final float threshold = thresholds[thresholdsBase + node];
					node = (attributeValue < threshold) ?
						attributes[attributesBase + 2 + 3 * node + 1] :
						attributes[attributesBase + 2 + 3 * node + 2];
				}
				final int j = node - Short.MIN_VALUE;
				c0 += probabilities[probabilitiesBase + j];
				c1 += probabilities[probabilitiesBase + j + 1];
				attributesBase += 2 + 3 * attrSize;
				thresholdsBase += attrSize;
				probabilitiesBase += probSize;
			}
		}

		final float invsum = 1f / (c0 + c1);
		distribution[0] = c0 * invsum;
		distribution[1] = c1 * invsum;
	}

	/**
	 * Applies the random forest to the given instance.
	 * This implements the general case for 3 classes.
	 */
	private void distributionForInstance_c3(final float[] instance,
		final float[] distribution)
	{
		float c0 = prior[0], c1 = prior[1], c2 = prior[2];
		final int numClasses = 3;
		int attributesBase = 0;
		int probabilitiesBase = 0;
		int depth = 0;
		for (; depth < numTreesOfHeight.length &&
			depth < COMPACT_STORAGE_MIN_HEIGHT - 1; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			if (depth == 0) // special case for trees of height 1
			{
				final int dataSize = 1;
				final int probSize = 2 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h1(instance, attributesBase);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					c2 += probabilities[probabilitiesBase +
						branchBits * numClasses + 2];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else if (depth == 1) // special case for trees of height 2
			{
				final int dataSize = 3;
				final int probSize = 4 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h2(instance, attributesBase);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					c2 += probabilities[probabilitiesBase +
						branchBits * numClasses + 2];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else if (depth == 2) // special case for trees of height 3
			{
				final int dataSize = 7;
				final int probSize = 8 * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree_h3(instance, attributesBase);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					c2 += probabilities[probabilitiesBase +
						branchBits * numClasses + 2];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
			else // general case
			{
				final int numLeafs = 2 << depth;
				final int dataSize = numLeafs - 1;
				final int probSize = numLeafs * numClasses;
				for (int tree = 0; tree < nh; ++tree) {
					final int branchBits =
						evaluateTree(instance, attributesBase, depth);
					c0 += probabilities[probabilitiesBase +
						branchBits * numClasses];
					c1 += probabilities[probabilitiesBase +
						branchBits * numClasses + 1];
					c2 += probabilities[probabilitiesBase +
						branchBits * numClasses + 2];
					attributesBase += dataSize;
					probabilitiesBase += probSize;
				}
			}
		}

		int thresholdsBase = attributesBase;
		for (; depth < numTreesOfHeight.length; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			for (int tree = 0; tree < nh; ++tree) {
				final int attrSize = attributes[attributesBase];
				final int probSize = attributes[attributesBase + 1];
				int node = 0;
				while (node >= 0) {
					final int attributeIndex =
						attributes[attributesBase + 2 + 3 * node];
					final float attributeValue = instance[attributeIndex];
					final float threshold = thresholds[thresholdsBase + node];
					node = (attributeValue < threshold) ?
						attributes[attributesBase + 2 + 3 * node + 1] :
						attributes[attributesBase + 2 + 3 * node + 2];
				}
				final int j = node - Short.MIN_VALUE;
				c0 += probabilities[probabilitiesBase + j];
				c1 += probabilities[probabilitiesBase + j + 1];
				c2 += probabilities[probabilitiesBase + j + 2];
				attributesBase += 2 + 3 * attrSize;
				thresholdsBase += attrSize;
				probabilitiesBase += probSize;
			}
		}

		final float invsum = 1f / (c0 + c1 + c2);
		distribution[0] = c0 * invsum;
		distribution[1] = c1 * invsum;
		distribution[2] = c2 * invsum;
	}

	/**
	 * Apply the random forest to the given instance, and return the leaf
	 * index for the resulting leaf probabilities.
	 *
	 * @param instance
	 * 	feature vector to evaluate the tree on.
	 * @param dataBase
	 * 	offset into attributes[], thresholds[] where the tree is placed.
	 * @param maxDepth
	 * 	maximum depth of any node in the tree.
	 *
	 * @return leaf index at maxDepth after evaluating the instance.
	 * (This can be multiplied by {@code numClasses} to get the index
	 * into leaf probabilities[] relative to start offset of the tree.)
	 */
	private int evaluateTree(final float[] instance, final int dataBase,
		final int maxDepth)
	{
		int branchBits = 0;
		for (int nodeIndex = 0, depth = 0; depth <= maxDepth; ++depth) {
			final int o = dataBase + nodeIndex;
			final int attributeIndex = attributes[o];
			if (attributeIndex < 0) {
				branchBits = branchBits << (1 + maxDepth - depth);
				break;
			}
			else {
				final float attributeValue = instance[attributeIndex];
				final float threshold = thresholds[o];
				final int branch = attributeValue < threshold ? 0 : 1;
				nodeIndex = (nodeIndex << 1) + branch + 1;
				branchBits = (branchBits << 1) + branch;
			}
		}
		return branchBits;
	}

	/**
	 * See {@link #evaluateTree}.
	 * This is a special case implementation for trees of height 1.
	 */
	private int evaluateTree_h1(final float[] instance, final int dataBase)
	{
		final int attributeIndex = attributes[dataBase];
		final float attributeValue = instance[attributeIndex];
		final float threshold = thresholds[dataBase];
		final int branchBits = attributeValue < threshold ? 0 : 1;
		return branchBits;
	}

	/**
	 * See {@link #evaluateTree}.
	 * This is a special case implementation for trees of height 2.
	 */
	private int evaluateTree_h2(final float[] instance, final int dataBase)
	{
		final int attributeIndex0 = attributes[dataBase];
		final float attributeValue0 = instance[attributeIndex0];
		final float threshold0 = thresholds[dataBase];

		int branchBits;
		int o;
		if (attributeValue0 < threshold0) {
			branchBits = 0;
			o = 1;
		}
		else {
			branchBits = 2;
			o = 2;
		}
		final int dataBase1 = dataBase + o;

		final int attributeIndex1 = attributes[dataBase1];
		if (attributeIndex1 < 0) return branchBits;
		final float attributeValue1 = instance[attributeIndex1];
		final float threshold1 = thresholds[dataBase1];
		if (attributeValue1 >= threshold1) branchBits += 1;

		return branchBits;
	}

	/**
	 * See {@link #evaluateTree}.
	 * This is a special case implementation for trees of height 3.
	 */
	private int evaluateTree_h3(final float[] instance, final int dataBase)
	{
		final int attributeIndex0 = attributes[dataBase];
		final float attributeValue0 = instance[attributeIndex0];
		final float threshold0 = thresholds[dataBase];

		int branchBits;
		int o;
		if (attributeValue0 < threshold0) {
			branchBits = 0;
			o = 1;
		}
		else {
			branchBits = 4;
			o = 2;
		}
		final int dataBase1 = dataBase + o;

		final int attributeIndex1 = attributes[dataBase1];
		if (attributeIndex1 < 0) return branchBits;
		final float attributeValue1 = instance[attributeIndex1];
		final float threshold1 = thresholds[dataBase1];
		if (attributeValue1 < threshold1) {
			o = o * 2 + 1;
		}
		else {
			o = o * 2 + 2;
			branchBits += 2;
		}
		final int dataBase2 = dataBase + o;

		final int attributeIndex2 = attributes[dataBase2];
		if (attributeIndex2 < 0) return branchBits;
		final float attributeValue2 = instance[attributeIndex2];
		final float threshold2 = thresholds[dataBase2];

		if (attributeValue2 >= threshold2) branchBits += 1;

		return branchBits;
	}

	/**
	 * For reference (unused): distributionForInstance() without considering any
	 * special cases.
	 * <p>
	 * Applies the random forest to the given instance. Writes the class
	 * probabilities to {@code distribution}.
	 *
	 * @param instance
	 * 	Instance / feature vector, array length must equal
	 *    {@code numberOfFeatures}.
	 * @param distribution
	 * 	This is the output buffer, array length must equal number of classes.
	 */
	private void generic_distributionForInstance(final float[] instance,
		final float[] distribution)
	{
		final int numClasses = this.numClasses;
		for (int i = 0; i < numClasses; ++i)
			distribution[i] = prior[i];
		int attributesBase = 0;
		int probabilitiesBase = 0;
		int depth = 0;
		for (; depth < numTreesOfHeight.length &&
			depth < COMPACT_STORAGE_MIN_HEIGHT - 1; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			final int numLeafs = 2 << depth;
			final int dataSize = numLeafs - 1;
			final int probSize = numLeafs * numClasses;
			for (int tree = 0; tree < nh; ++tree) {
				final int branchBits =
					evaluateTree(instance, attributesBase, depth);
				for (int k = 0; k < numClasses; k++)
					distribution[k] += probabilities[probabilitiesBase +
						branchBits * numClasses + k];
				attributesBase += dataSize;
				probabilitiesBase += probSize;
			}
		}
		int thresholdsBase = attributesBase;
		for (; depth < numTreesOfHeight.length; depth++) {
			final int nh = numTreesOfHeight[depth];
			if (nh == 0) continue;

			for (int tree = 0; tree < nh; ++tree) {
				final int attrSize = attributes[attributesBase];
				final int probSize = attributes[attributesBase + 1];
				int node = 0;
				while (node >= 0) {
					final int attributeIndex =
						attributes[attributesBase + 2 + 3 * node];
					final float attributeValue = instance[attributeIndex];
					final float threshold = thresholds[thresholdsBase + node];
					node = (attributeValue < threshold) ?
						attributes[attributesBase + 2 + 3 * node + 1] :
						attributes[attributesBase + 2 + 3 * node + 2];
				}
				final int j = node - Short.MIN_VALUE;
				for (int k = 0; k < numClasses; k++)
					distribution[k] += probabilities[probabilitiesBase + j + k];
				attributesBase += 2 + 3 * attrSize;
				thresholdsBase += attrSize;
				probabilitiesBase += probSize;
			}
		}
		ArrayUtils.normalize(distribution);
	}

	public int numberOfClasses() {
		return numClasses;
	}
}
