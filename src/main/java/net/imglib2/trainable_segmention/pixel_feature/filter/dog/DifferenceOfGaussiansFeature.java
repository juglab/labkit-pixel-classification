package net.imglib2.trainable_segmention.pixel_feature.filter.dog;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Difference of Gaussians (Group)")
public class DifferenceOfGaussiansFeature extends AbstractGroupFeatureOp {

	protected List<FeatureSetting> initFeatures() {
		List<FeatureSetting> features = new ArrayList<>();
		final double minimumSigma = 1;
		final double maximumSigma = 16;
		for (double sigma1 = minimumSigma; sigma1 <= maximumSigma; sigma1 *= 2)
			for (double sigma2 = minimumSigma; sigma2 < sigma1; sigma2 *= 2)
				features.add(SingleFeatures.differenceOfGaussians(sigma1, sigma2));
		return features;
	}
}
