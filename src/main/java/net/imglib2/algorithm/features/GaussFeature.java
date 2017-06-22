package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class GaussFeature extends FeatureGroup {

	private static final double[] SIGMAS = new double[]{1.0, 2.0, 4.0, 8.0, 16.0};

	private static List<Feature> initFeatures() {
		return Arrays.stream(SIGMAS)
				.mapToObj(SingleGaussFeature::new)
				.collect(Collectors.toList());
	}

	public GaussFeature() {
		super(initFeatures());
	}
}
