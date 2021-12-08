/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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
import sc.fiji.labkit.pixel_classification.utils.ArrayUtils;
import weka.core.Instance;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps around a {@link FastRandomForest} and exposes all the trees, tree
 * nodes, thresholds and parameters used for the classification task.
 * <p>
 * Java reflection is used to extract the parameter from the weka fast random
 * forest.
 */
public class TransparentRandomForest {

	private final List<TransparentRandomTree> trees;

	public TransparentRandomForest(List<TransparentRandomTree> trees) {
		this.trees = trees;
	}

	public static TransparentRandomForest forFastRandomForest(
		FastRandomForest original)
	{
		return new TransparentRandomForest(initTrees(original));
	}

	private static List<TransparentRandomTree> initTrees(FastRandomForest original) {
		// NB: Type of bagger is hr.irb.fastRandomForest.FastRfBagging
		Object bagger = ReflectionUtils.getPrivateField(original, "m_bagger", Object.class);
		if (bagger == null)
			return Collections.emptyList();
		// NB: Type of trees is hr.irb.fastRandomForest.FastRandomTree
		Object[] trees = ReflectionUtils.getPrivateField(bagger, "m_Classifiers", Object[].class);
		return Collections.unmodifiableList(Stream.of(trees).map(
			TransparentRandomTree::forFastRandomTree).collect(
				Collectors.toList()));
	}

	public List<TransparentRandomTree> trees() {
		return trees;
	}

	public int numberOfClasses() {
		return trees.isEmpty() ? 0 : trees.get(0).numberOfClasses();
	}

	public double[] distributionForInstance(Instance instance, int numberOfClasses) {
		double[] result = new double[numberOfClasses];
		for (TransparentRandomTree tree : trees)
			ArrayUtils.add(tree.distributionForInstance(instance), result);
		return ArrayUtils.normalize(result);
	}
}
