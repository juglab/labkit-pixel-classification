
package net.imglib2.trainable_segmentation.pixel_feature.filter.stats;

import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSetting;
import org.scijava.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

@Plugin(type = FeatureOp.class, label = "variance filters (group)")
public class VarianceFeature extends AbstractGroupFeatureOp {

	@Override
	protected List<FeatureSetting> initFeatures() {
		return globalSettings().sigmas().stream()
			.map(r -> new FeatureSetting(SingleVarianceFeature.class, "radius", r))
			.collect(Collectors.toList());
	}
}
