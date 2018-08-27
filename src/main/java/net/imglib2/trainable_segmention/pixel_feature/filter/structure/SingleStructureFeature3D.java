package net.imglib2.trainable_segmention.pixel_feature.filter.structure;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.DerivedNormalDistribution;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.EigenValues;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.plugin.Parameter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class SingleStructureFeature3D extends AbstractFeatureOp {

	@Parameter
	double sigma = 1.0;

	@Parameter
	double integrationScale = 1.0;

	@Override
	public int count() {
		return 3;
	}

	@Override
	public List<String> attributeLabels() {
		return Arrays.asList("Structure_largest_" + sigma + "_" + integrationScale,
				"Structure_middle_" + sigma + "_" + integrationScale,
				"Structure_smallest_" + sigma + "_" + integrationScale);
	}

	@Override
	public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
		final Interval targetInterval = output.get(0);
		Convolution<NumericType<?>> convolution = gaussConvolution();
		final Interval derivativeInterval = convolution.requiredSourceInterval( targetInterval );
		Img<DoubleType> products = products(derivatives(input, derivativeInterval));
		RandomAccessibleInterval<DoubleType> blurredProducts = Views.interval( products,
				RevampUtils.appendDimensionToInterval( targetInterval, 0, 5));
		convolution.process( products, blurredProducts );

		EigenValues.Vector3D v = new EigenValues.Vector3D();
		LoopBuilder.setImages( Views.collapse(blurredProducts), Views.collapse(Views.stack(output))).forEachPixel( (in, out) -> eigenValuePerPixel(v, in, out));
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		apply(input.original(), output);
	}

	private Convolution<NumericType<?>> gaussConvolution() {
		final Kernel1D gauss = Kernel1D.symmetric( Gauss3.halfkernels( new double[]{ integrationScale } ) )[0];
		return SeparableKernelConvolution.convolution(new Kernel1D[]{gauss, gauss, gauss});
	}

	private static void eigenValuePerPixel(EigenValues.Vector3D tmp, Composite<DoubleType> in, Composite<FloatType> out) {
		EigenValues.eigenvalues(tmp,
				in.get(0).getRealDouble(),
				in.get(1).getRealDouble(),
				in.get(2).getRealDouble(),
				in.get(3).getRealDouble(),
				in.get(4).getRealDouble(),
				in.get(5).getRealDouble() );
		EigenValues.abs(tmp);
		EigenValues.sort(tmp);
		out.get(0).setReal(tmp.x);
		out.get(1).setReal(tmp.y);
		out.get(2).setReal(tmp.z);
	}

	private Img<DoubleType> products(Img<DoubleType> input) {
		Interval interval = RevampUtils.removeLastDimension(input);
		Img<DoubleType> output = ops().create().img(RevampUtils.appendDimensionToInterval(interval, 0, 6), new DoubleType());
		LoopBuilder.setImages( Views.collapse(input), Views.collapse(output)).forEachPixel( SingleStructureFeature3D::productPerPixel );
		return output;
	}

	private static void productPerPixel(Composite<DoubleType> i, Composite<DoubleType> o) {
		double x = i.get(0).getRealDouble(), y = i.get(1).getRealDouble(), z = i.get(2).getRealDouble();
		o.get(0).setReal(x * x);
		o.get(1).setReal(x * y);
		o.get(2).setReal(x * z);
		o.get(3).setReal(y * y);
		o.get(4).setReal(y * z);
		o.get(5).setReal(z * z);
	}

	private Img<DoubleType> derivatives(RandomAccessible<FloatType> input, Interval derivativeInterval) {
		int n = input.numDimensions();
		Img<DoubleType> tmp = ops().create().img(RevampUtils.appendDimensionToInterval(derivativeInterval, 0, n - 1), new DoubleType());
		for (int i = 0; i < n; i++)
			derive(input, Views.hyperSlice(tmp, n, i), i);
		return tmp;
	}

	private void derive(RandomAccessible<FloatType> input, RandomAccessibleInterval<DoubleType> tmp, int d) {
		Kernel1D gauss = DerivedNormalDistribution.derivedGaussKernel( sigma, 0 );
		Kernel1D derived = DerivedNormalDistribution.derivedGaussKernel( sigma, 1 );
		Kernel1D[] kernels = IntStream.range(0, input.numDimensions())
				.mapToObj( i -> i == d ? derived : gauss).toArray(Kernel1D[]::new);
		SeparableKernelConvolution.convolution(kernels).process(input, tmp);
	}
}
