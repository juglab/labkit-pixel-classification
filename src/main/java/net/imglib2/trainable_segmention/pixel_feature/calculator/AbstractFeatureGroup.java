package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpEnvironment;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureJoiner;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

abstract class AbstractFeatureGroup implements FeatureGroup {

	protected final FeatureJoiner joiner;

	protected final FeatureSettings settings;

	protected AbstractFeatureGroup(OpEnvironment ops, FeatureSettings settings) {
		this.settings = settings;
		List<FeatureOp> featureOps = settings.features().stream()
				.map(x -> x.newInstance(ops, settings.globals())).collect(Collectors.toList());
		this.joiner = new FeatureJoiner(featureOps);
	}

	@Override
	public OpEnvironment ops() {
		return joiner.ops();
	}

	@Override
	public FeatureSettings settings() {
		return settings;
	}

	@Override
	public List<FeatureOp> features() {
		return joiner.features();
	}

	@Override
	public int count() {
		return joiner.count() * channelCount();
	}

	@Override
	public List<String> attributeLabels() {
		return prepend(settings.globals().imageType().channelNames(), joiner.attributeLabels());
	}


	// TODO: Why calculate hash code? Should this method be moved to FeatureSettings
	@Override
	public int hashCode() {
		return Objects.hash(getType(), attributeLabels());
	}

	// -- Helper methods --

	private int channelCount() {
		return settings.globals().imageType().channelCount();
	}

	private static List<String> prepend(List<String> prepend, List<String> labels) {
		return labels.stream()
				.flatMap(label -> prepend.stream().map(pre -> pre.isEmpty() ? label : pre + "_" + label))
				.collect(Collectors.toList());
	}
}
