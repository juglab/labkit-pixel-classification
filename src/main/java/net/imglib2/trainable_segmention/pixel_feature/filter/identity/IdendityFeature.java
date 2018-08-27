package net.imglib2.trainable_segmention.pixel_feature.filter.identity;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */

@Plugin(type = FeatureOp.class, label = "Original Image")
public class IdendityFeature extends AbstractFeatureOp {

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		RandomAccessible<FloatType> a = in;
		RandomAccessibleInterval<FloatType> b = out.get(0);
		Views.interval(Views.pair(a, b), b).forEach(p -> p.getB().set(p.getA()));
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		apply(input.original(), output);
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("original");
	}
}
