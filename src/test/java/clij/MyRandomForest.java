
package clij;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.util.Cast;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyRandomForest extends MajorityClassifier {

	public MyRandomForest(FastRandomForest original) {
		super(initTrees(original));
	}

	private static List<MyRandomTree> initTrees(FastRandomForest original) {
		Object bagger = ReflectionUtils.getPrivateField(original, "m_bagger");
		Object[] trees = Cast.unchecked(ReflectionUtils.getPrivateField(bagger, "m_Classifiers"));
		return Collections.unmodifiableList(Stream.of(trees).map(MyRandomTree::new).collect(
			Collectors.toList()));
	}

	public List<MyRandomTree> trees() {
		return Cast.unchecked(trees);
	}
}
