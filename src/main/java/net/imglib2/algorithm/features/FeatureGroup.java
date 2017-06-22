package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class FeatureGroup implements Feature {

	private final List<Feature> features;

	private final int count;

	public FeatureGroup(Feature... features) {
		this(Arrays.asList(features));
	}

	public FeatureGroup(List<Feature> features) {
		this.features = features;
		this.count = this.features.stream().mapToInt(Feature::count).sum();
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		if(out.size() != count)
			throw new IllegalArgumentException();
		int startIndex = 0;
		for(Feature feature : features) {
			int count = feature.count();
			feature.apply(in, out.subList(startIndex, startIndex + count));
			startIndex += count;
		}
	}

	@Override
	public List<String> attributeLabels() {
		List<String> labels = new ArrayList<>();
		features.stream().map(Feature::attributeLabels).forEach(labels::addAll);
		return labels;
	}
}
