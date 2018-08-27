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
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.DerivedNormalDistribution;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.plugin.Parameter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SingleLaplacianFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList( "Laplacian_" + sigma );
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		int n = globalSettings().numDimensions();
		List<RandomAccessibleInterval<DoubleType>> derivatives = IntStream.range(0, n)
				.mapToObj(d -> input.derivedGauss(sigma, order(n, d))).collect(Collectors.toList());
		LoopBuilder.setImages(Views.collapse(Views.stack(derivatives)), output.get(0)).forEachPixel( (x, sum) -> sum.setReal( sum( x, n ) ));
	}

	private int[] order(int n, int d) {
		return IntStream.range( 0, n ).map( i -> i == d ? 2 : 0).toArray();
	}

	private double sum(Composite<DoubleType> d, int n) {
		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += d.get(i).getRealDouble();
		}
		return sum;
	}
}
