package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public class GrayFeatureGroup implements FeatureGroup<FloatType> {

	private final FeatureJoiner joiner;

	GrayFeatureGroup(List<FeatureOp> features) {
		this.joiner = new FeatureJoiner(features);
	}

	@Override
	public int count() {
		return joiner.count();
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		joiner.apply(in, out);
	}

	@Override
	public List<String> attributeLabels() {
		return joiner.attributeLabels();
	}

	@Override
	public List<FeatureOp> features() {
		return joiner.features();
	}
}
