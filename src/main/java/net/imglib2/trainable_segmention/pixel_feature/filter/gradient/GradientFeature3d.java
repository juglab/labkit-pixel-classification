package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import org.scijava.plugin.Parameter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GradientFeature3d extends AbstractGroupFeatureOp {

	@Parameter
	private int minOrder = 1;

	@Parameter
	private int maxOrder = 5;

	protected List<FeatureSetting> initFeatures() {
		return globalSettings().sigmas().stream()
				.flatMap(sigma -> initFeaturesForSigma(sigma))
				.collect(Collectors.toList());
	}

	private Stream<FeatureSetting> initFeaturesForSigma(Double sigma) {
		return orders().stream().map(order -> new FeatureSetting(SingleGradientFeature3d.class,
				"sigma", sigma, "order", order));
	}

	private List<Integer> orders() {
		return IntStream.rangeClosed(minOrder, maxOrder).boxed().collect(Collectors.toList());
	}
}
