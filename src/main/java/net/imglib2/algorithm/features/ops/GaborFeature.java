package net.imglib2.algorithm.features.ops;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.Features;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class GaborFeature extends AbstractGroupFeatureOp {

	@Parameter
	private boolean legacyNormalize = false;

	@Override
	protected List<Feature> initFeatures() {
		int nAngles = 10;
		List<Feature> features = new ArrayList<>();
		for(int i=0; i < 2; i++)
			for(double gamma = 1; gamma >= 0.25; gamma /= 2)
				for(int frequency = 2; frequency<3; frequency ++)
				{
					final double psi = Math.PI / 2 * i;
					features.add(createGaborFeature(1.0, gamma, psi, frequency, nAngles));
				}
		// elongated filters in x- axis (sigma = [2.0 - 4.0], gamma = [1.0 - 2.0])
		for(int i=0; i < 2; i++)
			for(double sigma = 2.0; sigma <= 4.0; sigma *= 2)
				for(double gamma = 1.0; gamma <= 2.0; gamma *= 2)
					for(int frequency = 2; frequency<=3; frequency ++)
					{
						final double psi = Math.PI / 2 * i;
						features.add(createGaborFeature(sigma, gamma, psi, frequency, nAngles));
					}
		return features;
	}

	private Feature createGaborFeature(double v, double gamma, double psi, int frequency, int nAngles) {
		return Features.create(SingleGaborFeature.class, v, gamma, psi, frequency, nAngles, legacyNormalize);
	}
}
