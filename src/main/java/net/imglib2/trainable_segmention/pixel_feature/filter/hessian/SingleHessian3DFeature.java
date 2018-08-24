package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.img.Img;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.DerivedNormalDistribution;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Hessian")
public class SingleHessian3DFeature extends AbstractFeatureOp {

	@Parameter
	double sigma = 4.0;

	@Parameter
	boolean absoluteValues = true;

	@Override
	public int count() {
		return 3;
	}

	@Override
	public List<String> attributeLabels() {
		return Stream.of("largest", "middle", "smallest").map(x -> "Hessian_" + x + "_" + sigma + "_" + absoluteValues)
				.collect(Collectors.toList());
	}

	@Override
	public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
		calculateHessianOnChannel(input, Views.stack(output));
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 3;
	}

	private void calculateHessianOnChannel(RandomAccessible<FloatType> image, RandomAccessibleInterval<FloatType> out) {
		Interval secondDerivativeInterval = RevampUtils.removeLastDimension(out);
		RandomAccessibleInterval<RealComposite<DoubleType>> secondDerivatives =
				calculateSecondDerivatives(secondDerivativeInterval, image);

		EigenValues.Vector3D v = new EigenValues.Vector3D();
		LoopBuilder.setImages(secondDerivatives, Views.collapseReal(out)).forEachPixel(
				(derivatives, eigenValues) -> calculateEigenValues(v, derivatives, eigenValues));
	}

	private RandomAccessibleInterval<RealComposite<DoubleType>> calculateSecondDerivatives(
			Interval secondDerivativeInterval,
			RandomAccessible<FloatType> image)
	{
		Img<DoubleType> secondDerivatives = ops().create().img(RevampUtils.appendDimensionToInterval(secondDerivativeInterval, 0, 5), new DoubleType());
		List<RandomAccessibleInterval<DoubleType>> slices = RevampUtils.slices(secondDerivatives);
		differenciate(image, slices.get(0), 2, 0, 0);
		differenciate(image, slices.get(1), 1, 1, 0);
		differenciate(image, slices.get(2), 1, 0, 1);
		differenciate(image, slices.get(3), 0, 2, 0);
		differenciate(image, slices.get(4), 0, 1, 1);
		differenciate(image, slices.get(5), 0, 0, 2);
		return Views.collapseReal(secondDerivatives);
	}

	private void differenciate(RandomAccessible<FloatType> input, RandomAccessibleInterval<DoubleType> output, int... order) {
		Kernel1D[] kernels = Arrays.stream(order)
				.mapToObj(o -> DerivedNormalDistribution.derivedGaussKernel(sigma, o))
				.toArray(Kernel1D[]::new);
		SeparableKernelConvolution.convolution(kernels).process(input, output);
	}

	private void calculateEigenValues(EigenValues.Vector3D v, RealComposite<DoubleType> derivatives, RealComposite<FloatType> eigenValues) {
		EigenValues.eigenvalues(v,
				derivatives.get(0).getRealDouble(),
				derivatives.get(1).getRealDouble(),
				derivatives.get(2).getRealDouble(),
				derivatives.get(3).getRealDouble(),
				derivatives.get(4).getRealDouble(),
				derivatives.get(5).getRealDouble());
		if(absoluteValues)
			EigenValues.abs(v);
		EigenValues.sort(v);
		eigenValues.get(0).setReal(v.x);
		eigenValues.get(1).setReal(v.y);
		eigenValues.get(2).setReal(v.z);
	}
}
