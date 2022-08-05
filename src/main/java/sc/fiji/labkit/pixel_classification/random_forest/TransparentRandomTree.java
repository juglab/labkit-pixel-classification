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

package sc.fiji.labkit.pixel_classification.random_forest;

import weka.core.Instance;

/**
 * Wraps around a hr.irb.fastRandomForest.FastRandomTree and exposes all the
 * parameters (threshold, children and classification probabilities).
 * <p>
 * Java reflection is used to extract the parameter from the weka fast random
 * forest.
 */
public class TransparentRandomTree {

	private final int attribute;

	private final double threshold;

	private final TransparentRandomTree smallerChild;

	private final TransparentRandomTree biggerChild;

	private final double[] classProbabilities;

	private TransparentRandomTree(int attribute, double threshold,
		TransparentRandomTree smallerChild,
		TransparentRandomTree biggerChild,
		double[] classProbabilities)
	{
		this.attribute = attribute;
		this.threshold = threshold;
		this.smallerChild = smallerChild;
		this.biggerChild = biggerChild;
		this.classProbabilities = classProbabilities;
	}

	public static TransparentRandomTree leaf(double[] classProbabilities) {
		return new TransparentRandomTree(-1, Double.NaN, null, null, classProbabilities);
	}

	public static TransparentRandomTree node(int attribute, double threshold,
		TransparentRandomTree smallerChild,
		TransparentRandomTree biggerChild)
	{
		return new TransparentRandomTree(attribute, threshold, smallerChild,
			biggerChild, null);
	}

	/**
	 * @param fastRandomTree is expected to be of type
	 *          hr.irb.fastRandomForest.FastRandomTree
	 */
	public static TransparentRandomTree forFastRandomTree(Object fastRandomTree) {
		int attribute = ReflectionUtils.getPrivateField(fastRandomTree, "m_Attribute", Integer.class);
		if (attribute < 0) {
			double[] probs = ReflectionUtils.getPrivateField(fastRandomTree, "m_ClassProbs",
				double[].class);
			return leaf(probs);
		}
		else {
			double threshold = ReflectionUtils.getPrivateField(fastRandomTree, "m_SplitPoint",
				Double.class);
			Object[] successors = ReflectionUtils.getPrivateField(fastRandomTree, "m_Successors",
				Object[].class);
			return node(attribute, threshold, forFastRandomTree(successors[0]), forFastRandomTree(
				successors[1]));
		}
	}

	public int attributeIndex() {
		return attribute;
	}

	public double threshold() {
		return threshold;
	}

	/**
	 * Returns the sub tree that is used if the attribute value is smaller than the
	 * threshold.
	 */
	public TransparentRandomTree smallerChild() {
		return smallerChild;
	}

	/**
	 * Returns the sub tree that is used if the attribute value is greater or equal
	 * to the threshold.
	 */
	public TransparentRandomTree biggerChild() {
		return biggerChild;
	}

	public double[] classProbabilities() {
		return classProbabilities;
	}

	public double[] distributionForInstance(Instance instance) {
		if (!isLeaf()) {
			TransparentRandomTree child = instance.value(attribute) < threshold ? smallerChild
				: biggerChild;
			return child.distributionForInstance(instance);
		}
		else {
			return classProbabilities;
		}
	}

	/**
	 * Returns true if the tree is only one leafnode.
	 */
	public boolean isLeaf() {
		return attribute == -1;
	}

	public int height() {
		return isLeaf() ? 0 : 1 + Math.max(smallerChild().height(), biggerChild().height());
	}

	public int numberOfNodes() {
		return 1 + (isLeaf() ? 0 : smallerChild().numberOfNodes() + biggerChild().numberOfNodes());
	}

	public int numberOfLeafs() {
		return isLeaf() ? 1 : smallerChild().numberOfLeafs() + biggerChild().numberOfLeafs();
	}

	public int numberOfClasses() {
		if (isLeaf())
			return classProbabilities.length;
		else
			return smallerChild.numberOfClasses();
	}
}
