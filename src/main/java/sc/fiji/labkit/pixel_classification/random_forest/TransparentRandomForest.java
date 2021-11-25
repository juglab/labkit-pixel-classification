
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
