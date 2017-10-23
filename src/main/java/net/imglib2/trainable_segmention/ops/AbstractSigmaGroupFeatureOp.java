package net.imglib2.trainable_segmention.ops;

import net.imglib2.trainable_segmention.FeatureSetting;

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
		List<Double> sigmas = new ArrayList<>();
		if(includeZero)
			sigmas.add(0.0);
		sigmas.addAll(globalSettings().sigmas());
		return sigmas;
	}

	protected List<FeatureSetting> initFeatures() {
		Class<? extends FeatureOp> featureClass = getSingleFeatureClass();
		return initSigmas().stream()
				.map(sigma -> new FeatureSetting(featureClass, "sigma", sigma))
				.collect(Collectors.toList());
	}

	protected abstract Class<? extends FeatureOp> getSingleFeatureClass();
}
