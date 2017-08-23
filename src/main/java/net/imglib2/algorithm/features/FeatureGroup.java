package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public class FeatureGroup {

	private final FeatureJoiner joiner;

	FeatureGroup(List<FeatureOp> features) {
		this.joiner = new FeatureJoiner(features);
	}

	public int count() {
		return joiner.count();
	}

	public void apply(List<RandomAccessible<FloatType>> in, List<RandomAccessibleInterval<FloatType>> out) {
		joiner.apply(in, out);
	}

	public List<String> attributeLabels() {
		return joiner.attributeLabels();
	}

	public List<FeatureOp> features() {
		return joiner.features();
	}
}
