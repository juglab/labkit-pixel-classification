package net.imglib2.algorithm.features;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class LipschitzFeature extends FeatureGroup {

	private static List<Feature> initFeatures() {
		return Arrays.stream(new double[]{5, 10, 15, 20, 25})
				.mapToObj(slope -> new SingleLipschitz2dFeature(true, true, slope))
				.collect(Collectors.toList());
	}

	public LipschitzFeature() {
		super(initFeatures());
	}
}
