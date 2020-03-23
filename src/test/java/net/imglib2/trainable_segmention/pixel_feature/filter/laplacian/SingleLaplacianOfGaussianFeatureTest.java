
package net.imglib2.trainable_segmention.pixel_feature.filter.laplacian;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SingleLaplacianOfGaussianFeatureTest {

	private final double sigma = 3.0;

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleLaplacianOfGaussianFeature.class, "sigma", sigma)
		.build();

	@Test
	public void testApply() {
		RandomAccessible<FloatType> input = Utils.dirac2d();
		RandomAccessibleInterval<FloatType> output = ArrayImgs.floats(10, 10);
		calculator.apply(input, Views.addDimension(output, 0, 0));
		RandomAccessibleInterval<FloatType> expected = Utils.create2dImage(output,
			(x, y) -> laplacianOfGaussian(sigma, x, y));
		Utils.assertImagesEqual(40, expected, output);
	}

	@Test
	public void testAttributes() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Arrays.asList("laplacian of gaussian sigma=3.0");
		assertEquals(expected, attributeLabels);
	}

	private double laplacianOfGaussian(double sigma, double x, double y) {
		double r_square = x * x + y * y;
		return (r_square / Math.pow(sigma, 4) - 2 * Math.pow(sigma, -2)) * Utils.gauss(sigma, x, y);
	}
}
