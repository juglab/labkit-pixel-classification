package net.imglib2.algorithm.features.ops;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractGroupFeatureOp extends AbstractFeatureOp {

	protected FeatureGroup featureGroup = Features.group();

	@Override
	public void initialize() {
		featureGroup = Features.group(initFeatures());
	}

	protected abstract List<FeatureOp> initFeatures();

	@Override
	public int count() {
		return featureGroup.count();
	}

	@Override
	public List<String> attributeLabels() {
		return featureGroup.attributeLabels();
	}

	@Override
	public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
		featureGroup.apply(input, output);
	}
}
