
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class SingleGaussianGradientMagnitudeFeatureTest {

	public SingleGaussianGradientMagnitudeFeatureTest(boolean useGpu) {
		this.calculator.setUseGPU(useGpu);
	}

	@Parameterized.Parameters(name = "useGpu = {0}")
	public static List<Boolean> data() {
		return Arrays.asList(false, true);
	}

	private final double sigma = 3.0;

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleGaussianGradientMagnitudeFeature.class, "sigma", 3.0)
		.build();

	@Test
	public void testApply2d() {
		RandomAccessibleInterval<FloatType> result = ArrayImgs.floats(10, 10);
		calculator.apply(Utils.dirac2d(), Views.addDimension(result, 0, 0));
		RandomAccessibleInterval<FloatType> expected = Utils.create2dImage(result,
			(x, y) -> gradientMagnitudeOfGaussian(sigma, x, y));
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.004);
	}

	@Test
	public void testAttributeLabels() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Collections.singletonList("gaussian gradient magnitude sigma=3.0");
		assertEquals(expected, attributeLabels);
	}

	private double gradientMagnitudeOfGaussian(double sigma, double x, double y) {
		double r = Math.sqrt(x * x + y * y);
		return r / (sigma * sigma) * Utils.gauss(sigma, x, y);
	}
}
