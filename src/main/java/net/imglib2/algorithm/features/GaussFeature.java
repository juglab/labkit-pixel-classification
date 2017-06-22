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
public class GaussFeature implements Feature {

	List<Double> sigmas = Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0);

	@Override
	public int count() {
		return sigmas.size();
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		try {
			for (int i = 0; i < sigmas.size(); i++)
				Gauss3.gauss(sigmas.get(i) * 0.4, in, out.get(i));
		} catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> attributeLabels() {
		return sigmas.stream().map(sigma -> "Gaussian_blur_" + sigma).collect(Collectors.toList());
	}

}
