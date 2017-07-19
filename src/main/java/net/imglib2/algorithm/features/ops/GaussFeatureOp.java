package net.imglib2.algorithm.features.ops;

import com.google.gson.annotations.Expose;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class GaussFeatureOp extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	public GaussFeatureOp() {}

	public GaussFeatureOp(double sigma) {
		this.sigma = sigma;
	}

	@Override
	public int count() {
			return 1;
		}

	@Override
	public List<String> attributeLabels() {
			return Collections.singletonList("Gaussian_blur_" + sigma);
		}

	@Override
	public void apply(RandomAccessible<FloatType> input, List<RandomAccessibleInterval<FloatType>> output) {
		try {
			Gauss3.gauss(sigma * 0.4, input, output.get(0));
		} catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
	}
}
