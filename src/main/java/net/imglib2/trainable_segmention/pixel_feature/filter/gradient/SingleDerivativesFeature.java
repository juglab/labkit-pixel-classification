package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Plugin(type = FeatureOp.class, label = "Derivatives")
public class SingleDerivativesFeature extends AbstractFeatureOp {

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
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		int[] orders = IntStream.range(0, globalSettings().numDimensions()).map(ignore -> order).toArray();
		RandomAccessibleInterval<? extends RealType<?>> derivative = input.derivedGauss(sigma, orders);
		LoopBuilder.setImages(derivative, output.get(0)).forEachPixel( (i, o) -> o.setReal(i.getRealFloat()));
	}
}
