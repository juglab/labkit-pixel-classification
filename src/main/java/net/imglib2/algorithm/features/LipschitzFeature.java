package net.imglib2.algorithm.features;

import net.imglib2.algorithm.features.ops.SingleLipschitzFeature;

import java.util.*;
import java.util.stream.Collectors;

import static net.imglib2.algorithm.features.RevampUtils.nCopies;

/**
 * @author Matthias Arzt
 */
public class LipschitzFeature {

	private LipschitzFeature() {
		// prevent from being instantiated
	}

	public static Feature group(long border) {
		return new FeatureGroup(initFeatures(border));
	}

	private static List<Feature> initFeatures(long border) {
		return Arrays.stream(new double[]{5, 10, 15, 20, 25})
				.mapToObj(slope -> single(slope, border))
				.collect(Collectors.toList());
	}

	public static Feature single(double slope, long border) {
		return Features.create(SingleLipschitzFeature.class, slope, border);
	}

}
