
package net.imglib2.trainable_segmention.pixel_feature.filter.structure;

import clij.CLIJEigenvalues;
import clij.CLIJLoopBuilder;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJMultiChannelImage;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.composite.RealComposite;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.algorithm.convolution.Convolution;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import preview.net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.EigenValuesSymmetric3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.plugin.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver.iterator;

@Plugin(type = FeatureOp.class, label = "structure tensor eigenvalues")
public class SingleStructureTensorEigenvaluesFeature extends AbstractFeatureOp {

	@Parameter
	double sigma = 1.0;

	@Parameter
	double integrationScale = 1.0;

	@Override
	public int count() {
		return globalSettings().numDimensions();
	}

	@Override
	public List<String> attributeLabels() {
		List<String> prefix = getPrefix();
		return prefix.stream().map(s -> "structure tensor - " + s + " eigenvalue sigma=" + sigma +
			" integrationScale=" + integrationScale).collect(Collectors.toList());
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		final Interval targetInterval = output.get(0);
		Convolution<NumericType<?>> convolution = gaussConvolution();
		final Interval derivativeInterval = convolution.requiredSourceInterval(targetInterval);
		RandomAccessibleInterval<DoubleType> derivatives = derivatives(input, derivativeInterval);
		RandomAccessibleInterval<DoubleType> products = products(derivatives);
		RandomAccessibleInterval<DoubleType> blurredProducts = Views.interval(products,
			Intervals.addDimension(targetInterval, products.min(products.numDimensions() - 1), products
				.max(products.numDimensions() - 1)));
		convolution.process(products, blurredProducts);
		EigenValues<DoubleType, FloatType> eigenvalueComputer = globalSettings().numDimensions() == 3
			? new EigenValuesSymmetric3D() : EigenValues.symmetric2D();
		LoopBuilder.setImages(Views.collapse(blurredProducts), RevampUtils.vectorizeStack(output))
			.forEachPixel(eigenvalueComputer::compute);
	}

	private Convolution<NumericType<?>> gaussConvolution() {
		final Kernel1D[] gauss = globalSettings().pixelSize().stream()
			.map(pixelSize -> gaussKernel(integrationScale / pixelSize))
			.toArray(Kernel1D[]::new);
		return SeparableKernelConvolution.convolution(gauss);
	}

	private Kernel1D gaussKernel(double v) {
		return Kernel1D.symmetric(Gauss3.halfkernels(new double[] { v })[0]);
	}

	private RandomAccessibleInterval<DoubleType> products(
		RandomAccessibleInterval<DoubleType> derivatives)
	{
		Interval interval = RevampUtils.removeLastDimension(derivatives);
		Interval outputInterval = Intervals.addDimension(interval, 0, getNumberOfProducts() - 1);
		RandomAccessibleInterval<DoubleType> output = ops().create().img(outputInterval,
			new DoubleType());
		LoopBuilder.setImages(Views.collapseReal(derivatives), Views.collapseReal(output)).forEachPixel(
			getProductPerPixelAction());
		return output;
	}

	private RandomAccessibleInterval<DoubleType> derivatives(FeatureInput input,
		Interval derivativeInterval)
	{
		RandomAccessibleInterval<DoubleType> gauss = ops().create().img(Intervals.expand(
			derivativeInterval, 1));
		List<Double> pixelSize = globalSettings().pixelSize();
		double[] sigmas = pixelSize.stream().mapToDouble(p -> sigma / p).toArray();
		RandomAccessible<FloatType> original = input.original();
		Gauss3.gauss(sigmas, original, gauss);
		int n = derivativeInterval.numDimensions();
		Img<DoubleType> tmp = ops().create().img(RevampUtils.appendDimensionToInterval(
			derivativeInterval, 0, n - 1), new DoubleType());
		for (int i = 0; i < n; i++)
			derive(gauss, Views.hyperSlice(tmp, n, i), i, pixelSize.get(i));
		return tmp;
	}

	private void derive(RandomAccessible<? extends RealType<?>> input,
		RandomAccessibleInterval<? extends RealType<?>> tmp, int d, double pixelSize)
	{
		final RandomAccessibleInterval<? extends RealType<?>> back = Views.interval(input, Intervals
			.translate(tmp, -1, d));
		final RandomAccessibleInterval<? extends RealType<?>> front = Views.interval(input, Intervals
			.translate(tmp, 1, d));
		final double factor = 0.5 / pixelSize;
		LoopBuilder.setImages(tmp, back, front).forEachPixel((r, b, f) -> {
			r.setReal((f.getRealDouble() - b.getRealDouble()) * factor);
		});
	}

	// -- dimension specific helper methods --

	private List<String> getPrefix() {
		return globalSettings().numDimensions() == 3 ? Arrays.asList("largest", "middle", "smallest")
			: Arrays.asList("largest", "smallest");
	}

	private int getNumberOfProducts() {
		return globalSettings().numDimensions() == 3 ? 6 : 3;
	}

	private BiConsumer<RealComposite<DoubleType>, RealComposite<DoubleType>>
		getProductPerPixelAction()
	{
		return globalSettings().numDimensions() == 3
			? SingleStructureTensorEigenvaluesFeature::productPerPixel3d
			: SingleStructureTensorEigenvaluesFeature::productPerPixel2d;
	}

	private static void productPerPixel3d(Composite<DoubleType> i, Composite<DoubleType> o) {
		double x = i.get(0).getRealDouble(), y = i.get(1).getRealDouble(), z = i.get(2).getRealDouble();
		o.get(0).setReal(x * x);
		o.get(1).setReal(x * y);
		o.get(2).setReal(x * z);
		o.get(3).setReal(y * y);
		o.get(4).setReal(y * z);
		o.get(5).setReal(z * z);
	}

	private static void productPerPixel2d(Composite<DoubleType> i, Composite<DoubleType> o) {
		double x = i.get(0).getRealDouble(), y = i.get(1).getRealDouble();
		o.get(0).setReal(x * x);
		o.get(1).setReal(x * y);
		o.get(2).setReal(y * y);
	}

	// -- CLIJ implementation --


	@Override
	public void prefetch(CLIJFeatureInput input) {
		double[] integrationSigma = globalSettings().pixelSize().stream().mapToDouble(p -> integrationScale / p).toArray();
		double[] gaussSigma = globalSettings().pixelSize().stream().mapToDouble(p -> sigma / p).toArray();
		long[] border = DoubleStream.of(integrationSigma).mapToLong(sigma -> (long)(4 * sigma)).toArray();
		Interval derivativeInterval = Intervals.expand(input.targetInterval(), border);
		for (int d = 0; d < globalSettings().numDimensions(); d++)
			input.prefetchDerivative(gaussSigma[d], d, derivativeInterval);
	}

	@Override
	public void apply(CLIJFeatureInput input, List<CLIJView> output) {
		double[] integrationSigma = globalSettings().pixelSize().stream().mapToDouble(p -> integrationScale / p).toArray();
		long[] border = DoubleStream.of(integrationSigma).mapToLong(sigma -> (long)(4 * sigma)).toArray();
		List<CLIJView> derivatives = derivatives(input, border);
		try (
			CLIJMultiChannelImage products = products(input.clij(), derivatives);
			CLIJMultiChannelImage blurredProducts = blur(input.clij(), products.channels(), integrationSigma, border);
		) {
			CLIJEigenvalues.symmetric(input.clij(), blurredProducts.channels(), output);
		}
	}

	private List<CLIJView> derivatives(CLIJFeatureInput input, long[] border) {
		double[] gaussSigma = globalSettings().pixelSize().stream().mapToDouble(p -> sigma / p).toArray();
		Interval derivativeInterval = Intervals.expand(input.targetInterval(), border);
		int n = globalSettings().numDimensions();
		List<CLIJView> derivatives = new ArrayList<>(3);
		for (int d = 0; d < n; d++)
			derivatives.add(input.derivative(gaussSigma[d], d, derivativeInterval));
		return derivatives;
	}

	private CLIJMultiChannelImage products(CLIJ2 clij, List<CLIJView> derivatives) {
		int n = derivatives.size();
		int numProducts = n * (n + 1) / 2;
		long[] dimensions = Intervals.dimensionsAsLongArray(derivatives.get(0).interval());
		CLIJLoopBuilder loopBuilder = CLIJLoopBuilder.clij(clij);
		StringJoiner operation = new StringJoiner("; ");
		for (int i = 0; i < derivatives.size(); i++)
			loopBuilder.addInput("derivative" + i, derivatives.get(i));
		CLIJMultiChannelImage products = new CLIJMultiChannelImage(clij, dimensions, numProducts);
		Iterator<CLIJView> iterator = products.channels().iterator();
		for (int i = 0; i < derivatives.size(); i++)
			for (int j = i; j < derivatives.size(); j++) {
				loopBuilder.addOutput("product" + i + j, iterator.next());
				operation.add("product" + i + j + " = derivative" + i + " * derivative" + j);
			}
		loopBuilder.forEachPixel(operation.toString());
		return products;
	}

	private CLIJMultiChannelImage blur(CLIJ2 clij, List<CLIJView> products, double[] integrationSigma, long[] border) {
		long[] dimensions = Intervals.dimensionsAsLongArray(products.get(0).interval());
		try (
			ClearCLBuffer tmpInput = clij.create(dimensions, NativeTypeEnum.Float);
			ClearCLBuffer tmpOutput = clij.create(dimensions, NativeTypeEnum.Float);
		){
			long[] outputDimensions = shrink(dimensions, border);
			CLIJMultiChannelImage blurred = new CLIJMultiChannelImage(clij, outputDimensions, products.size());
			List<CLIJView> blurredChannels = blurred.channels();
			for (int i = 0; i < products.size(); i++) {
				CLIJView input = products.get(i);
				CLIJView output = blurredChannels.get(i);
				CLIJCopy.copy(clij, input, CLIJView.wrap(tmpInput));
				if(integrationSigma.length == 3)
					clij.gaussianBlur(tmpInput, tmpOutput, integrationSigma[0], integrationSigma[1], integrationSigma[2]);
				else
					clij.gaussianBlur(tmpInput, tmpOutput, integrationSigma[0], integrationSigma[1]);
				CLIJCopy.copy(clij, CLIJView.shrink(tmpOutput, border), output);
			}
			return blurred;
		}
	}

	private long[] shrink(long[] dimensions, long[] border) {
		long[] result = new long[dimensions.length];
		for (int i = 0; i < result.length; i++) result[i] = dimensions[i] - 2 * border[i];
		return result;
	}
}
