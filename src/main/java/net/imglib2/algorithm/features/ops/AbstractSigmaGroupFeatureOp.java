package net.imglib2.algorithm.features.ops;

import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.GlobalSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractSigmaGroupFeatureOp extends AbstractGroupFeatureOp {

	private final boolean includeZero;

	protected AbstractSigmaGroupFeatureOp(boolean includeZero) {
		this.includeZero = includeZero;
	}

	private List<Double> initSigmas() {
		GlobalSettings settings = globalSettings();
		List<Double> sigmas = new ArrayList<>();
		if(includeZero)
			sigmas.add(0.0);
		for(double sigma = settings.minSigma(), maxSigma = settings.maxSigma(); sigma <= maxSigma; sigma *= 2.0)
			sigmas.add(sigma);
		return sigmas;
	}

	protected List<FeatureOp> initFeatures() {
		Class<? extends FeatureOp> featureClass = getSingleFeatureClass();
		return initSigmas().stream()
				.map(sigma -> Features.create(featureClass, globalSettings(), sigma))
				.collect(Collectors.toList());
	}

	protected abstract Class<? extends FeatureOp> getSingleFeatureClass();
}
