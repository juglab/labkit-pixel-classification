
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import clij.CLIJEigenvalues;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.Composite;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(type = FeatureOp.class, label = "hessian eigenvalues")
public class SingleHessianEigenvaluesFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return globalSettings().numDimensions();
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		if (globalSettings().numDimensions() == 2)
			apply2d(input, output);
		else if (globalSettings().numDimensions() == 3)
			apply3d(input, output);
		else throw new AssertionError();
	}

	@Override
	public List<String> attributeLabels() {
		List<String> prefix = globalSettings().numDimensions() == 2 ? Arrays.asList("largest",
			"smallest") : Arrays.asList("largest", "middle", "smallest");
		return prefix.stream().map(s -> "hessian - " + s + " eigenvalue sigma=" + sigma)
			.collect(Collectors.toList());
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2 || globals.numDimensions() == 3;
	}

	private void apply2d(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		RandomAccessibleInterval<DoubleType> dxx = input.derivedGauss(sigma, 2, 0);
		RandomAccessibleInterval<DoubleType> dxy = input.derivedGauss(sigma, 1, 1);
		RandomAccessibleInterval<DoubleType> dyy = input.derivedGauss(sigma, 0, 2);
		RandomAccessibleInterval<FloatType> larger = output.get(0);
		RandomAccessibleInterval<FloatType> smaller = output.get(1);
		LoopBuilder.setImages(dxx, dxy, dyy, larger, smaller).multiThreaded().forEachPixel(
			(s_xx, s_yx, s_yy, l, s) -> calculateHessianPerPixel(s_xx.getRealDouble(), s_yx
				.getRealDouble(), s_yy.getRealDouble(), l, s));
	}

	private static void calculateHessianPerPixel(
		double s_xx, double s_xy, double s_yy, FloatType largerEigenvalue, FloatType smallerEigenvalue)
	{
		final double trace = s_xx + s_yy;
		float l = (float) (trace / 2.0 + Math.sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) /
			2.0);
		largerEigenvalue.set(l);
		float s = (float) (trace / 2.0 - Math.sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) /
			2.0);
		smallerEigenvalue.set(s);
	}

	private void apply3d(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		RandomAccessibleInterval<DoubleType> dxx = input.derivedGauss(sigma, 2, 0, 0);
		RandomAccessibleInterval<DoubleType> dxy = input.derivedGauss(sigma, 1, 1, 0);
		RandomAccessibleInterval<DoubleType> dxz = input.derivedGauss(sigma, 1, 0, 1);
		RandomAccessibleInterval<DoubleType> dyy = input.derivedGauss(sigma, 0, 2, 0);
		RandomAccessibleInterval<DoubleType> dyz = input.derivedGauss(sigma, 0, 1, 1);
		RandomAccessibleInterval<DoubleType> dzz = input.derivedGauss(sigma, 0, 0, 2);
		RandomAccessibleInterval<Composite<DoubleType>> derivative = RevampUtils.vectorizeStack(dxx,
			dxy, dxz, dyy, dyz, dzz);
		RandomAccessibleInterval<Composite<FloatType>> eigenvalues = RevampUtils.vectorizeStack(output);
		EigenValues<DoubleType, FloatType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(derivative, eigenvalues).forEachPixel(calculator::compute);
	}

	@Override
	public void prefetch(CLIJFeatureInput input) {
		final Interval interval = input.targetInterval();
		final int n = interval.numDimensions();
		for(int d1 = 0; d1 < n; d1++)
			for(int d2 = d1; d2 < n; d2++)
				input.prefetchSecondDerivative(sigma, d1, d2, interval);
	}

	@Override
	public void apply(CLIJFeatureInput input, List<CLIJView> output) {
		final Interval interval = input.targetInterval();
		final int n = interval.numDimensions();
		final List<CLIJView> derivatives = new ArrayList<>();
		for(int d1 = 0; d1 < n; d1++)
			for(int d2 = d1; d2 < n; d2++)
				derivatives.add(input.secondDerivative(sigma, d1, d2, interval));
		List<CLIJView> eigenvalues = new ArrayList<>(output);
		CLIJEigenvalues.symmetric(input.clij(), derivatives, eigenvalues);
	}
}
