
package clij;

import net.imglib2.util.Cast;
import weka.core.Instance;

public class MyRandomTree implements SimpleClassifier {

	private final int attribute;

	private final double splitPoint;

	private final MyRandomTree childLessThan;

	private final MyRandomTree childGreaterThan;

	private final double[] classProbabilities;

	public MyRandomTree(Object original) {
		this.attribute = Cast.<Integer> unchecked(ReflectionUtils.getPrivateField(original,
			"m_Attribute"));
		if (isLeaf()) {
			this.splitPoint = Double.NaN;
			this.childLessThan = null;
			this.childGreaterThan = null;
			this.classProbabilities = Cast.unchecked(ReflectionUtils.getPrivateField(original,
				"m_ClassProbs"));
		}
		else {
			this.splitPoint = Cast.<Double> unchecked(ReflectionUtils.getPrivateField(original,
				"m_SplitPoint"));
			Object[] sucessors = Cast.unchecked(ReflectionUtils.getPrivateField(original,
				"m_Successors"));
			this.childLessThan = new MyRandomTree(sucessors[0]);
			this.childGreaterThan = new MyRandomTree(sucessors[1]);
			this.classProbabilities = null;
		}
	}

	public int attributeIndex() {
		return attribute;
	}

	public double threshold() {
		return splitPoint;
	}

	public MyRandomTree childLessThan() {
		return childLessThan;
	}

	public MyRandomTree childGreaterThan() {
		return childGreaterThan;
	}

	public double[] classProbabilities() {
		return classProbabilities;
	}

	@Override
	public double[] distributionForInstance(Instance instance) {
		if (!isLeaf()) {
			MyRandomTree child = instance.value(attribute) < splitPoint ? childLessThan
				: childGreaterThan;
			return child.distributionForInstance(instance);
		}
		else {
			return classProbabilities;
		}
	}

	public boolean isLeaf() {
		return attribute == -1;
	}
}
