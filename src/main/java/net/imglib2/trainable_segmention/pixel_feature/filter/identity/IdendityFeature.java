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
	public List<RandomAccessibleInterval<FloatType>> apply(FeatureInput in) {
		return Collections.singletonList( Views.interval(in.original(), in.targetInterval()) );
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		copy(input.original(), output.get(0));
	}

	private void copy(RandomAccessible<FloatType> a, RandomAccessibleInterval<FloatType> b) {
		Views.interval(Views.pair(a, b), b).forEach(p -> p.getB().set(p.getA()));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("original");
	}
}
