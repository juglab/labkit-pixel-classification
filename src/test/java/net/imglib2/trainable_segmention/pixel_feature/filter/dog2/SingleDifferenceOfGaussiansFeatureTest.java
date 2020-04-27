
package net.imglib2.trainable_segmention.pixel_feature.filter.dog2;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.utils.CpuGpuRunner;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(CpuGpuRunner.class)
public class SingleDifferenceOfGaussiansFeatureTest {

	public SingleDifferenceOfGaussiansFeatureTest(boolean useGpu) {
		this.calculator.setUseGpu(useGpu);
		this.calculator3d.setUseGpu(useGpu);
	}

	private double sigma1 = 2.0;

	private double sigma2 = 4.0;

	private FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeatures(SingleFeatures.differenceOfGaussians(sigma1, sigma2))
		.build();

	private FeatureCalculator calculator3d = FeatureCalculator.default2d()
		.dimensions(3)
		.addFeatures(SingleFeatures.differenceOfGaussians(sigma1, sigma2))
		.build();

	@Test
	public void testDifferenceOfGaussians() {
		RandomAccessible<FloatType> dirac = Utils.dirac2d();
		FinalInterval interval = Intervals.createMinMax(-10, -10, 10, 10);
		RandomAccessibleInterval<FloatType> result = Views.hyperSlice(calculator.apply(dirac, interval),
			2, 0);
		RandomAccessibleInterval<FloatType> expected = Utils.create2dImage(interval, (x, y) -> Utils
			.gauss(sigma1, x, y) - Utils.gauss(sigma2, x, y));
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.001);
	}

	@Test
	public void test3d() {
		RandomAccessible<FloatType> input = Utils.dirac(3);
		FinalInterval interval = Intervals.createMinMax(-10, -10, -10, 10, 10, 10);
		RandomAccessibleInterval<FloatType> result = Views.hyperSlice(calculator3d.apply(input,
			interval), 3, 0);
		RandomAccessibleInterval<FloatType> expected = Utils.create3dImage(interval, ((x, y, z) -> Utils
			.gauss(
				sigma1, x, y, z) - Utils.gauss(sigma2, x, y, z)));
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.001);
	}

	@Test
	public void testAttribute() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Collections.singletonList(
			"difference of gaussians sigma1=2.0 sigma2=4.0");
		assertEquals(expected, attributeLabels);
	}
}
