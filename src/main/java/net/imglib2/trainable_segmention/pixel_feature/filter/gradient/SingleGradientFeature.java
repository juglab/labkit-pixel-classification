
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by arzt on 19.07.17.
 */
@Plugin(type = FeatureOp.class, label = "Gradient")
public class SingleGradientFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Edges_" + sigma);
	}

	@Override
	public void apply(RandomAccessible<FloatType> input,
		List<RandomAccessibleInterval<FloatType>> output)
	{
		int n = input.numDimensions();
		Img<DoubleType> tmp = ops().create().img(RevampUtils.appendDimensionToInterval(output.get(0), 0,
			n - 1), new DoubleType());
		for (int i = 0; i < n; i++) {
			derive(input, Views.hyperSlice(tmp, n, i), i);
		}
		LoopBuilder.setImages(Views.collapse(tmp), output.get(0)).forEachPixel((in, out) -> {
			double sum = 0;
			for (int i = 0; i < n; i++) {
				double x = in.get(i).getRealDouble();
				sum += x * x;
			}
			out.setReal(Math.sqrt(sum));
		});
	}

	private void derive(RandomAccessible<FloatType> input, RandomAccessibleInterval<DoubleType> tmp,
		int d)
	{
		Kernel1D gauss = DerivedNormalDistribution.derivedGaussKernel(sigma, 0);
		Kernel1D derived = DerivedNormalDistribution.derivedGaussKernel(sigma, 1);
		Kernel1D[] kernels = IntStream.range(0, input.numDimensions())
			.mapToObj(i -> i == d ? derived : gauss).toArray(Kernel1D[]::new);
		SeparableKernelConvolution.convolution(kernels).process(input, tmp);
	}
}
