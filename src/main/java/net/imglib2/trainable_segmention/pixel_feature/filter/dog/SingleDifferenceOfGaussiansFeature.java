
package net.imglib2.trainable_segmention.pixel_feature.filter.dog;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Difference of Gaussians")
public class SingleDifferenceOfGaussiansFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma1 = 2.0;

	@Parameter
	private double sigma2 = 4.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(FeatureInput in, List<RandomAccessibleInterval<FloatType>> out) {
		dog(in, out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Difference_of_gaussians_" + sigma1 + "_" + sigma2);
	}

	private void dog(FeatureInput in, RandomAccessibleInterval<FloatType> out) {
		LoopBuilder.setImages(in.gauss(sigma2 * 0.4), in.gauss(sigma1 * 0.4), out)
			.forEachPixel((a, b, r) -> r.setReal(a.getRealFloat() - b.getRealFloat()));
	}
}
