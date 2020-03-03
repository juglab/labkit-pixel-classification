
package net.imglib2.trainable_segmention.pixel_feature.filter.gauss;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "gaussian blur")
public class SingleGaussianBlurFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("gaussian blur sigma=" + sigma);
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		LoopBuilder.setImages(input.gauss(sigma), output.get(0)).forEachPixel((i, o) -> o.setReal(i
			.getRealFloat()));
	}

	@Override
	public void applyWithCLIJ(CLIJ2 clij, FeatureInput input, List<CLIJView> output) {
		Interval interval = input.targetInterval();
		double[] sigmas = globalSettings().pixelSize().stream().mapToDouble(p -> sigma / p).toArray();
		long[] border = DoubleStream.of(sigmas).mapToLong(x -> (long) (4 * x)).toArray();
		try (
			ClearCLBuffer inputClBuffer = clij.push(Views.interval(input.original(), Intervals.expand(
				interval, border)));
			ClearCLBuffer tmp = clij.create(inputClBuffer);)
		{
			clij.gaussianBlur3D(inputClBuffer, tmp, sigmas[0], sigmas[1], sigmas[2]);
			CLIJCopy.copy(clij, CLIJView.shrink(tmp, border), output.get(0));
		}
	}
}
