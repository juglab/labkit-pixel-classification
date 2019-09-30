
package net.imglib2.trainable_segmention.pixel_feature.filter.dog;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
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
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		dog(in, out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Difference_of_gaussians_" + sigma1 + "_" + sigma2);
	}

	private void dog(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		try {
			Img<FloatType> tmp = ops().create().img(out);
			Gauss3.gauss(sigma1 * 0.4, in, tmp);
			Gauss3.gauss(sigma2 * 0.4, in, out);
			Views.interval(Views.pair(tmp, out), out).forEach(p -> p.getB().sub(p.getA()));
		}
		catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
	}
}
