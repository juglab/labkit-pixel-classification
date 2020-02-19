
package net.imglib2.trainable_segmention.pixel_feature.filter.dog2;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
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
@Plugin(type = FeatureOp.class, label = "difference of gaussians")
public class SingleDifferenceOfGaussiansFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma1 = 1.0;

	@Parameter
	private double sigma2 = 2.0;

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
		return Collections.singletonList("difference of gaussians sigma1=" + sigma1 + " sigma2=" +
			sigma2);
	}

	private void dog(FeatureInput in, RandomAccessibleInterval<FloatType> result) {
		subtract(in.gauss(sigma1), in.gauss(sigma2), result);
	}

	private void subtract(RandomAccessibleInterval<DoubleType> minuend,
		RandomAccessibleInterval<DoubleType> subtrahend, RandomAccessibleInterval<FloatType> result)
	{
		LoopBuilder.setImages(minuend, subtrahend, result)
			.forEachPixel((a, b, r) -> r.setReal(a.getRealFloat() - b.getRealFloat()));
	}
}
