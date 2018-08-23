package net.imglib2.trainable_segmention.pixel_feature.filter.laplacian;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.DerivedNormalDistribution;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.plugin.Parameter;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class SingleLaplacianFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList( "Laplacian_" + sigma );
	}

	@Override
	public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
		Interval interval = output.get(0);
		int n = input.numDimensions();
		Img<DoubleType> derivatives = ops().create().img(RevampUtils.appendDimensionToInterval(interval, 0, n), new DoubleType());
		List<RandomAccessibleInterval<DoubleType>> d = RevampUtils.slices(derivatives);
		for(int i = 0; i < n; i++)
			differentiate(input, i, d.get(i));
		LoopBuilder.setImages(Views.collapse(derivatives), output.get(0)).forEachPixel( (x, sum) -> sum.setReal( sum( x, n ) ));
	}

	private double sum(Composite<DoubleType> d, int n) {
		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += d.get(i).getRealDouble();
		}
		return sum;
	}

	private void differentiate(RandomAccessible<FloatType> input, int d, RandomAccessibleInterval<DoubleType> output) {
		Kernel1D gauss = DerivedNormalDistribution.derivedGaussKernel(sigma, 0);
		Kernel1D second_derivative = DerivedNormalDistribution.derivedGaussKernel(sigma, 2);
		Kernel1D[] kernels = IntStream.range( 0, input.numDimensions() )
				.mapToObj( i -> i == d ? second_derivative : gauss).toArray(Kernel1D[]::new);
		SeparableKernelConvolution.convolution( kernels ).process( input, output );
	}
}
