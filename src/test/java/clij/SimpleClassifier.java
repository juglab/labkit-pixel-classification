
package clij;

import weka.core.Instance;

public interface SimpleClassifier {

	double[] distributionForInstance(final Instance instance);

	default int classifyInstance(Instance instance) {
		return ArrayUtils.findMax(distributionForInstance(instance));
	}

}
