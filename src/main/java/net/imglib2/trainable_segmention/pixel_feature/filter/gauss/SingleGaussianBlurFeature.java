
package net.imglib2.trainable_segmention.pixel_feature.filter.gauss;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

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
		input.prefetchGauss(sigma, input.targetInterval());
	}

	@Override
	public void apply(CLIJFeatureInput input, List<CLIJView> output) {
		CLIJCopy.copy(input.gpuApi(), input.gauss(sigma, input.targetInterval()), output.get(0));
	}
}
