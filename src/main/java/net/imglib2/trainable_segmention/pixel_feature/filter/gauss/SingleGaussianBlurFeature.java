
package net.imglib2.trainable_segmention.pixel_feature.filter.gauss;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
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
	public void prefetch(CLIJFeatureInput input) {
		input.prefetchOriginal(requiredInput(input));
	}

	private FinalInterval requiredInput(CLIJFeatureInput input) {
		return Intervals.expand(input.targetInterval(), borders());
	}

	@Override
	public void apply(CLIJFeatureInput input, List<CLIJView> output) {
		CLIJ2 clij = input.clij();
		try(
				ClearCLBuffer inputCL = copyView(clij, input.original(requiredInput(input)));
				ClearCLBuffer tmp = clij.create(inputCL);
		)
		{
			double[] sigmas = sigmas();
			clij.gaussianBlur3D(inputCL, tmp, sigmas[0], sigmas[1], sigmas[2]);
			CLIJCopy.copy(clij, CLIJView.shrink(tmp, borders()), output.get(0));
		}
	}

	private long[] borders() {
		return DoubleStream.of(sigmas()).mapToLong(x -> (long)(4 * x)).toArray();
	}

	private double[] sigmas() {
		return globalSettings().pixelSize().stream().mapToDouble(p -> sigma / p).toArray();
	}

	private ClearCLBuffer copyView(CLIJ2 clij, CLIJView inputClBuffer) {
		ClearCLBuffer buffer = clij.create(Intervals.dimensionsAsLongArray(inputClBuffer.interval()));
		CLIJCopy.copy(clij, inputClBuffer, CLIJView.wrap(buffer));
		return buffer;
	}
}
