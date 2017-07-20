package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.Features;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class DifferenceOfGaussiansFeature extends AbstractGroupFeatureOp {

	protected List<FeatureOp> initFeatures() {
		List<FeatureOp> features = new ArrayList<>();
		final double minimumSigma = 1;
		final double maximumSigma = 16;
		for (double sigma1 = minimumSigma; sigma1 <= maximumSigma; sigma1 *= 2)
			for (double sigma2 = minimumSigma; sigma2 < sigma1; sigma2 *= 2)
				features.add(Features.create(SingleDifferenceOfGaussiansFeature.class, sigma1, sigma2));
		return features;
	}
}
