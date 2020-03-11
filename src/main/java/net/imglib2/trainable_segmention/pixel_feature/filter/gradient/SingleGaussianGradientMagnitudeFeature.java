
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import clij.CLIJLoopBuilder;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "gaussian gradient magnitude")
public class SingleGaussianGradientMagnitudeFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("gaussian gradient magnitude sigma=" + sigma);
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		final int n = globalSettings().numDimensions();
		if (n == 2)
			apply2d(input, output.get(0));
		else if (n == 3)
			apply3d(input, output.get(0));
		else
			throw new AssertionError();
	}

	private void apply3d(FeatureInput input, RandomAccessibleInterval<FloatType> output) {
		RandomAccessibleInterval<DoubleType> dx = derive(input, 0);
		RandomAccessibleInterval<DoubleType> dy = derive(input, 1);
		RandomAccessibleInterval<DoubleType> dz = derive(input, 2);
		LoopBuilder.setImages(dx, dy, dz, output).forEachPixel(
			(x, y, z, o) -> o.setReal(magnitude(x.getRealDouble(), y.getRealDouble(), z
				.getRealDouble())));
	}

	private void apply2d(FeatureInput input, RandomAccessibleInterval<FloatType> output) {
		RandomAccessibleInterval<DoubleType> dx = derive(input, 0);
		RandomAccessibleInterval<DoubleType> dy = derive(input, 1);
		LoopBuilder.setImages(dx, dy, output).forEachPixel(
			(x, y, o) -> o.setReal(magnitude(x.getRealDouble(), y.getRealDouble())));
	}

	private static double magnitude(double x, double y) {
		return Math.sqrt(square(x) + square(y));
	}

	private static double magnitude(double x, double y, double z) {
		return Math.sqrt(square(x) + square(y) + square(z));
	}

	private static double square(double x) {
		return x * x;
	}

	private RandomAccessibleInterval<DoubleType> derive(FeatureInput input, int d) {
		int[] orders = new int[globalSettings().numDimensions()];
		orders[d] = 1;
		return input.derivedGauss(sigma, orders);
	}

	@Override
	public void prefetch(CLIJFeatureInput input) {
		for (int d = 0; d < globalSettings().numDimensions(); d++)
			input.prefetchDerivative(sigma, d, input.targetInterval());
	}

	@Override
	public void apply(CLIJFeatureInput input, List<CLIJView> output) {
		boolean is3d = globalSettings().numDimensions() == 3;
		CLIJ2 clij = input.clij();
		CLIJLoopBuilder loopBuilder = CLIJLoopBuilder.clij(clij);
		loopBuilder.addInput("dx", input.derivative(sigma, 0, input.targetInterval()));
		loopBuilder.addInput("dy", input.derivative(sigma, 1, input.targetInterval()));
		if (is3d)
			loopBuilder.addInput("dz", input.derivative(sigma, 2, input.targetInterval()));
		loopBuilder.addOutput("output", output.get(0));
		String operation = is3d ? "output = sqrt(dx * dx + dy * dy + dz * dz)"
			: "output = sqrt(dx * dx + dy * dy)";
		loopBuilder.forEachPixel(operation);
	}
}
