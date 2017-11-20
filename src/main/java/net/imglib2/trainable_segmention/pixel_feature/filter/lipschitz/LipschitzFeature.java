package net.imglib2.trainable_segmention.pixel_feature.filter.lipschitz;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class LipschitzFeature extends AbstractGroupFeatureOp {

	@Parameter
	private long border;

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	protected List<FeatureSetting> initFeatures() {
		return Arrays.stream(new double[]{5, 10, 15, 20, 25})
				.mapToObj(slope -> SingleFeatures.lipschitz(slope, border))
				.collect(Collectors.toList());
	}
}
