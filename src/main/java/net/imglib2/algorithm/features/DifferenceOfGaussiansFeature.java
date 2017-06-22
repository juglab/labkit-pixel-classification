package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Mattias Arzt
 */
public class DifferenceOfGaussiansFeature implements Feature {

	private final List<Double> sigmas1 = new ArrayList<>();
	private final List<Double> sigmas2 = new ArrayList<>();

	public DifferenceOfGaussiansFeature() {
		final double minimumSigma = 1;
		final double maximumSigma = 16;
		for (double sigma1 = minimumSigma; sigma1 <= maximumSigma; sigma1 *= 2)
			for (double sigma2 = minimumSigma; sigma2 < sigma1; sigma2 *= 2) {
				sigmas1.add(sigma1);
				this.sigmas2.add(sigma2);
			}
	}

	@Override
	public int count() {
		return sigmas1.size();
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		for (int i = 0; i < out.size(); i++)
			dog(in, out.get(i), sigmas1.get(i) * 0.4, sigmas2.get(i) * 0.4);
	}

	@Override
	public List<String> attributeLabels() {
		return IntStream.range(0, sigmas1.size())
				.mapToObj(i -> "Difference_of_gaussians_" + sigmas1.get(i) + "_" + sigmas2.get(i))
				.collect(Collectors.toList());
	}

	private void dog(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out, Double sigma1, Double sigma2) {
		try {
			Img<FloatType> tmp = RevampUtils.ops().create().img(out);
			Gauss3.gauss(sigma1, in, tmp);
			Gauss3.gauss(sigma2, in, out);
			Views.interval(Views.pair(tmp, out), out).forEach(p -> p.getB().sub(p.getA()));
		} catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
	}
}
