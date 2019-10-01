
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.RandomAccessibleInterval;
import preview.net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Plugin(type = FeatureOp.class, label = "Derivatives")
public class SingleDerivativesFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma;

	@Parameter
	private int order;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Derivatives_" + order + "_" + order + "_" + order + "_" +
			sigma);
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		int[] orders = IntStream.range(0, globalSettings().numDimensions()).map(ignore -> order)
			.toArray();
		RandomAccessibleInterval<? extends RealType<?>> derivative = input.derivedGauss(sigma, orders);
		copy(derivative, output);
	}

	private void copy(RandomAccessibleInterval<? extends RealType<?>> source,
		List<RandomAccessibleInterval<FloatType>> target)
	{
		LoopBuilder.setImages(source, target.get(0)).forEachPixel((i, o) -> o.setReal(i
			.getRealFloat()));
	}
}
