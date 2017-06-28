package net.imglib2.algorithm.features;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class LipschitzFeature extends FeatureGroup {

	private static List<Feature> initFeatures(long border) {
		return Arrays.stream(new double[]{5, 10, 15, 20, 25})
				.mapToObj(slope -> new SingleLipschitzFeature(slope, border))
				.collect(Collectors.toList());
	}

	public LipschitzFeature(long border) {
		super(initFeatures(border));
	}
}
