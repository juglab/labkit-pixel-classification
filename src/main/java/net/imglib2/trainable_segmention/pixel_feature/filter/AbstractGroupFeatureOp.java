package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractGroupFeatureOp extends AbstractFeatureOp {

	protected FeatureJoiner featureGroup = new FeatureJoiner(Collections.emptyList());

	@Override
	public void initialize() {
		featureGroup = new FeatureJoiner(initFeatures().stream().map(x -> x.newInstance(ops(), globalSettings()))
				.collect(Collectors.toList()));
	}

	protected abstract List<FeatureSetting> initFeatures();

	@Override
	public int count() {
		return featureGroup.count();
	}

	@Override
	public List<String> attributeLabels() {
		return featureGroup.attributeLabels();
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		featureGroup.apply(input, output);
	}
}
