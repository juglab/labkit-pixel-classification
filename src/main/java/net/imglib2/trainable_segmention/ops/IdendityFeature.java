package net.imglib2.trainable_segmention.ops;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
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
	public List<String> attributeLabels() {
		return Collections.singletonList("original");
	}
}
