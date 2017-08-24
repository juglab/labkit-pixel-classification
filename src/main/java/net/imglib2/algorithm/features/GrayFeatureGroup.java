package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public class GrayFeatureGroup implements FeatureGroup {

	private final FeatureJoiner joiner;

	GrayFeatureGroup(List<FeatureOp> features) {
		this.joiner = new FeatureJoiner(features);
	}

	@Override
	public int count() {
		return joiner.count();
	}

	@Override
	public void apply(RandomAccessible<?> in, List<RandomAccessibleInterval<FloatType>> out) {
		joiner.apply(RevampUtils.castRandomAccessible(in, getType()), out);
	}

	@Override
	public List<String> attributeLabels() {
		return joiner.attributeLabels();
	}

	@Override
	public List<FeatureOp> features() {
		return joiner.features();
	}

	@Override
	public Class<FloatType> getType() {
		return FloatType.class;
	}
}
