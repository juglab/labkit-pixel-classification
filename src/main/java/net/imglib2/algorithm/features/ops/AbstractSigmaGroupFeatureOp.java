package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.Features;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractSigmaGroupFeatureOp extends AbstractGroupFeatureOp {

	private final double[] SIGMAS = initSigmas();

	protected double[] initSigmas() {
		return new double[]{1.0, 2.0, 4.0, 8.0, 16.0};
	}

	protected List<FeatureOp> initFeatures() {
		Class<? extends FeatureOp> featureClass = getSingleFeatureClass();
		return Arrays.stream(SIGMAS)
				.mapToObj(sigma -> Features.create(featureClass, sigma))
				.collect(Collectors.toList());
	}

	protected abstract Class<? extends FeatureOp> getSingleFeatureClass();
}
