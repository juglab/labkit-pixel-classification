
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.img.Img;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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
		return Stream.of("largest", "middle", "smallest").map(x -> "Hessian_" + x + "_" + sigma +
			"_true")
			.collect(Collectors.toList());
	}

	@Override
	public void apply(RandomAccessible<FloatType> input,
		List<RandomAccessibleInterval<FloatType>> output)
	{
		calculateHessianOnChannel(input, Views.stack(output), sigma);
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 3;
	}

	private void calculateHessianOnChannel(RandomAccessible<FloatType> image,
		RandomAccessibleInterval<FloatType> out, double sigma)
	{
		double[] sigmas = { 0.4 * sigma, 0.4 * sigma, 0.4 * sigma };

		Interval secondDerivativeInterval = RevampUtils.removeLastDimension(out);
		Interval firstDerivativeInterval = Intervals.expand(secondDerivativeInterval, 1);
		Interval blurredInterval = Intervals.expand(firstDerivativeInterval, 1);

		RandomAccessibleInterval<FloatType> blurred = RevampUtils.gauss(ops(), image, blurredInterval,
			sigmas);
		RandomAccessibleInterval<FloatType> dx = derive(blurred, firstDerivativeInterval, 0);
		RandomAccessibleInterval<FloatType> dy = derive(blurred, firstDerivativeInterval, 1);
		RandomAccessibleInterval<FloatType> dz = derive(blurred, firstDerivativeInterval, 2);
		RandomAccessibleInterval<RealComposite<FloatType>> secondDerivatives =
			calculateSecondDerivatives(secondDerivativeInterval, dx, dy, dz);

		RandomAccessibleInterval<RealComposite<FloatType>> eigenValues = Views.collapseReal(out);
		Views.interval(Views.pair(secondDerivatives, eigenValues), eigenValues).forEach(
			p -> calculateEigenValues(p.getA(), p.getB()));
	}

	private RandomAccessibleInterval<RealComposite<FloatType>> calculateSecondDerivatives(
		Interval secondDerivativeInterval,
		RandomAccessibleInterval<FloatType> dx,
		RandomAccessibleInterval<FloatType> dy,
		RandomAccessibleInterval<FloatType> dz)
	{
		Img<FloatType> secondDerivatives = ops().create().img(RevampUtils.appendDimensionToInterval(
			secondDerivativeInterval, 0, 5), new FloatType());
		List<RandomAccessibleInterval<FloatType>> slices = RevampUtils.slices(secondDerivatives);
		PartialDerivative.gradientCentralDifference(dx, slices.get(0), 0);
		PartialDerivative.gradientCentralDifference(dx, slices.get(1), 1);
		PartialDerivative.gradientCentralDifference(dx, slices.get(2), 2);
		PartialDerivative.gradientCentralDifference(dy, slices.get(3), 1);
		PartialDerivative.gradientCentralDifference(dy, slices.get(4), 2);
		PartialDerivative.gradientCentralDifference(dz, slices.get(5), 2);
		return Views.collapseReal(secondDerivatives);
	}

	private void calculateEigenValues(RealComposite<FloatType> derivatives,
		RealComposite<FloatType> eigenValues)
	{
		EigenValues.Vector3D v = new EigenValues.Vector3D();
		EigenValues.eigenvalues(v,
			derivatives.get(0).getRealDouble(),
			derivatives.get(1).getRealDouble(),
			derivatives.get(2).getRealDouble(),
			derivatives.get(3).getRealDouble(),
			derivatives.get(4).getRealDouble(),
			derivatives.get(5).getRealDouble());
		if (absoluteValues)
			EigenValues.abs(v);
		EigenValues.sort(v);
		eigenValues.get(0).setReal(v.x);
		eigenValues.get(1).setReal(v.y);
		eigenValues.get(2).setReal(v.z);
	}

	// -- Helper methods --

	private RandomAccessibleInterval<FloatType> derive(RandomAccessible<FloatType> source,
		Interval interval, int dimension)
	{
		Img<FloatType> target = ops().create().img(interval, new FloatType());
		PartialDerivative.gradientCentralDifference(source, target, dimension);
		return target;
	}
}
