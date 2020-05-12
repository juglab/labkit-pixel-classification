
package net.imglib2.trainable_segmentation.gpu.random_forest;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.util.Cast;
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

	public TransparentRandomForest(FastRandomForest original) {
		this.trees = initTrees(original);
	}

	private static List<TransparentRandomTree> initTrees(FastRandomForest original) {
		Object bagger = ReflectionUtils.getPrivateField(original, "m_bagger");
		Object[] trees = Cast.unchecked(ReflectionUtils.getPrivateField(bagger, "m_Classifiers"));
		return Collections.unmodifiableList(Stream.of(trees).map(TransparentRandomTree::new).collect(
			Collectors.toList()));
	}

	public List<TransparentRandomTree> trees() {
		return Cast.unchecked(trees);
	}

	public double[] distributionForInstance(Instance instance) {
		double[] result = new double[2];
		for (TransparentRandomTree tree : trees)
			ArrayUtils.add(tree.distributionForInstance(instance), result);
		return ArrayUtils.normalize(result);
	}
}
