package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class FeatureGroup implements Feature {

	private final List<FeatureOp> features;

	private final int count;

	FeatureGroup(List<FeatureOp> features) {
		this.features = features.stream().flatMap(this::toStream).collect(Collectors.toList());
		this.count = this.features.stream().mapToInt(Feature::count).sum();
	}

	private Stream<? extends FeatureOp> toStream(FeatureOp f) {
		return (f instanceof FeatureGroup) ? ((FeatureGroup) f).features.stream() : Stream.of(f);
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

	public List<Feature> features() {
		return Collections.unmodifiableList(features);
	}
}
