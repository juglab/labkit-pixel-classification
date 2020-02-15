
package net.imglib2.trainable_segmention.pixel_feature.filter.gabor;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Gabor (Group)")
public class GaborFeature extends AbstractGroupFeatureOp {

	@Parameter
	private boolean legacyNormalize = false;

	@Override
	protected List<FeatureSetting> initFeatures() {
		int nAngles = 10;
		List<FeatureSetting> features = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			for (double gamma = 1; gamma >= 0.25; gamma /= 2)
				for (int frequency = 2; frequency < 3; frequency++) {
					final double psi = Math.PI / 2 * i;
					features.add(createGaborFeature(1.0, gamma, psi, frequency, nAngles));
				}
		// elongated filters in x- axis (sigma = [2.0 - 4.0], gamma = [1.0 - 2.0])
		for (int i = 0; i < 2; i++)
			for (double sigma = 2.0; sigma <= 4.0; sigma *= 2)
				for (double gamma = 1.0; gamma <= 2.0; gamma *= 2)
					for (int frequency = 2; frequency <= 3; frequency++) {
						final double psi = Math.PI / 2 * i;
						features.add(createGaborFeature(sigma, gamma, psi, frequency, nAngles));
					}
		return features;
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	private FeatureSetting createGaborFeature(double sigma, double gamma, double psi, int frequency,
		int nAngles)
	{
		return legacyNormalize ? SingleFeatures.legacyGabor(sigma, gamma, psi, frequency, nAngles)
			: SingleFeatures.gabor(sigma, gamma, psi, frequency, nAngles);
	}
}
