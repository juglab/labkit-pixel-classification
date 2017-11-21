package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Hessian (Group)")
public class Hessian3DFeature extends AbstractGroupFeatureOp {

	@Parameter
	private boolean absoluteValues = true;

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 3;
	}

	@Override
	protected List<FeatureSetting> initFeatures() {
		return globalSettings().sigmas().stream()
				.map(sigma -> SingleFeatures.hessian3d(sigma, absoluteValues))
				.collect(Collectors.toList());
	}
}
