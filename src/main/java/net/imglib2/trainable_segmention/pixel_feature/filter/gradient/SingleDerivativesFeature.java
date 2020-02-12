
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		return Collections.singletonList("Derivatives_" + order + "_" + order + "_" + order + "_" +
			sigma);
	}

	@Override
	public void apply(RandomAccessible<FloatType> input,
		List<RandomAccessibleInterval<FloatType>> output)
	{
		Kernel1D kernel = DerivedNormalDistribution.derivedGaussKernel(sigma, order);
		Kernel1D[] kernels = new Kernel1D[globalSettings().numDimensions()];
		Arrays.fill(kernels, kernel);
		SeparableKernelConvolution.convolve(kernels, input, output.get(0));
	}
}
