
package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.haesleinhuepf.clij2.CLIJ2;
import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by arzt on 23.08.17.
 */
public class FeatureJoiner {

	private final GlobalSettings settings;

	private final OpEnvironment ops;

	private final List<FeatureOp> features;

	private final int count;

	public FeatureJoiner(List<FeatureOp> features) {
		this.settings = checkGlobalSettings(features);
		this.ops = checkOps(features);
		this.features = features;
		this.count = this.features.stream().mapToInt(FeatureOp::count).sum();
	}

	private GlobalSettings checkGlobalSettings(List<FeatureOp> features) {
		if (features.isEmpty())
			return null;
		GlobalSettings settings = features.get(0).globalSettings();
		boolean allEqual =
			features.stream().allMatch(f -> settings.equals(f.globalSettings()));
		if (!allEqual)
			throw new IllegalArgumentException(
				"All features in a feature group must use the same global settings");
		return settings;
	}

	private OpEnvironment checkOps(List<FeatureOp> features) {
		if (features.isEmpty())
			return null;
		return features.get(0).ops();
	}

	public GlobalSettings globalSettings() {
		return settings;
	}

	public int count() {
		return count;
	}

	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		genericApply(output, (featureOp, o) -> featureOp.apply(input, o));
	}

	public void applyWithCLIJ(CLIJ2 clij, FeatureInput input, List<CLIJView> output) {
		genericApply(output, (featureOp, o) -> featureOp.applyWithCLIJ(clij, input, o));
	}

	public <T> void genericApply(List<T> output, BiConsumer<FeatureOp, List<T>> applyFeature) {
		if (output.size() != count)
			throw new IllegalArgumentException();
		int startIndex = 0;
		for (FeatureOp feature : features) {
			int count = feature.count();
			applyFeature.accept(feature, output.subList(startIndex, startIndex + count));
			startIndex += count;
		}
	}

	public List<String> attributeLabels() {
		List<String> labels = new ArrayList<>();
		features.stream().map(FeatureOp::attributeLabels).forEach(labels::addAll);
		return labels;
	}

	public List<FeatureOp> features() {
		return Collections.unmodifiableList(features);
	}

	public OpEnvironment ops() {
		return ops;
	}
}
