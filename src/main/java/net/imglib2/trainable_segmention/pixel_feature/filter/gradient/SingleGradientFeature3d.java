package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;

import java.util.Collections;
import java.util.List;

public class SingleGradientFeature3d extends AbstractFeatureOp {

	@Parameter
	private double sigma;

	@Parameter
	private int order;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Derivatives_" + order + "_" + order + "_" + order + "_" + sigma);
	}

	@Override
	public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {

	}
}
