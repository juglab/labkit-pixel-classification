
package clij;

import weka.core.Instance;

import java.util.List;

public class MajorityClassifier implements SimpleClassifier {

	protected final List<? extends SimpleClassifier> trees;

	public MajorityClassifier(List<? extends SimpleClassifier> trees) {
		this.trees = trees;
	}

	public double[] distributionForInstance(Instance instance) {
		double[] result = new double[2];
		for (SimpleClassifier tree : trees)
			ArrayUtils.add(tree.distributionForInstance(instance), result);
		return ArrayUtils.normalize(result);
	}

}
