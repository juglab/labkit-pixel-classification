package net.imglib2.trainable_segmention.pixel_feature.filter.dog;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Difference of Gaussians (Group)")
public class DifferenceOfGaussiansFeature extends AbstractFeatureOp {

	@Parameter
	private double scaleFactor = 0.4;

	private List<Double> sigmas;
	private List<Pair<Double, Double>> sigmaPairs;

	private List<Pair<Double, Double>> sigmaPairs() {
		List<Pair<Double, Double>> sigmaPairs = new ArrayList<>();
		for (double sigma2 : sigmas)
			for (double sigma1 : sigmas)
				if(sigma1 < sigma2)
					sigmaPairs.add(new ValuePair<>(sigma1, sigma2));
		return sigmaPairs;
	}

	@Override
	public void initialize() {
		sigmas = globalSettings().sigmas().stream()
				.map(radius -> scaleFactor * radius)
				.collect(Collectors.toList());
		sigmaPairs = sigmaPairs();
	}

	@Override
	public int count() {
		return sigmaPairs.size();
	}

	@Override
	public List<String> attributeLabels() {
		return sigmaPairs.stream().map(pair -> "Difference_of_gaussians_" + pair.getA() + "_" + pair.getB())
				.collect(Collectors.toList());
	}

	@Override
	public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
		Interval interval = new FinalInterval(output.get(0));
		Map<Double, RandomAccessibleInterval<FloatType>> gausses = calculateGausses(input, interval);
		calculateDifferences(gausses, output);
	}

	private void calculateDifferences(Map<Double, RandomAccessibleInterval<FloatType>> gausses, List<RandomAccessibleInterval<FloatType>> output) {
		for (int i = 0; i < output.size(); i++) {
			Pair<Double, Double> sigma1and2 = sigmaPairs.get(i);
			RandomAccessibleInterval<FloatType> target = output.get(i);
			subtract(gausses.get(sigma1and2.getB()), gausses.get(sigma1and2.getA()), target);
		}
	}

	private Map<Double, RandomAccessibleInterval<FloatType>> calculateGausses(RandomAccessible<FloatType> input, Interval interval) {
		Map<Double, RandomAccessibleInterval<FloatType>> gausses = new HashMap<>();
		for (double sigma : sigmas)
			gausses.put(sigma, gauss(input, interval, sigma));
		return gausses;
	}

	private static void subtract(RandomAccessibleInterval<FloatType> minuend, RandomAccessibleInterval<FloatType> subtrahend, RandomAccessibleInterval<FloatType> target) {
		LoopBuilder.setImages(minuend, subtrahend, target).forEachPixel(
				(m, s, t) -> t.setReal(m.getRealFloat() - s.getRealFloat())
		);
	}

	private RandomAccessibleInterval<FloatType> gauss(RandomAccessible<FloatType> input, Interval interval, double sigma) {
		Img<FloatType> result = ops().create().img(interval, new FloatType());
		RevampUtils.wrapException(() -> Gauss3.gauss(sigma, input, result) );
		return result;
	}
}
