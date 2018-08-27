package net.imglib2.trainable_segmention.pixel_feature.filter.structure;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureFeature3D extends AbstractGroupFeatureOp {

	@Override
	protected List<FeatureSetting> initFeatures() {
		return globalSettings().sigmas().stream().flatMap(
				this::initFeaturesForSigma
		).collect(Collectors.toList());
	}

	private Stream<FeatureSetting> initFeaturesForSigma(Double sigma) {
		return Stream.of(1.0, 3.0).map( integrationScale ->
				new FeatureSetting(SingleStructureFeature3D.class, "sigma", sigma,
						"integrationScale", integrationScale));
	}
}
