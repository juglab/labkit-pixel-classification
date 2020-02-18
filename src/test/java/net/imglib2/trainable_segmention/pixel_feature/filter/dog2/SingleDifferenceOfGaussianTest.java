
package net.imglib2.trainable_segmention.pixel_feature.filter.dog2;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import static org.junit.Assert.assertEquals;

public class SingleDifferenceOfGaussianTest {

	private double sigma1 = 2.0;

	private double sigma2 = 4.0;

	private FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeatures(SingleFeatures.differenceOfGaussians(sigma1, sigma2))
		.build();

	@Test
	public void testDifferenceOfGaussians() {
		RandomAccessible<FloatType> dirac = dirac2d();
		FinalInterval interval = Intervals.createMinMax(-10, -10, 10, 10);
		RandomAccessibleInterval<FloatType> result = Views.hyperSlice(calculator.apply(dirac, interval),
			2, 0);
		RandomAccessibleInterval<FloatType> expected = create2dImage(interval, (x, y) -> gauss(sigma1,
			x, y) - gauss(sigma2, x, y));
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.001);
	}

	@Test
	public void testAttribute() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Collections.singletonList(
			"difference of gaussians sigma1=2.0 sigma2=4.0");
		assertEquals(expected, attributeLabels);
	}

	private double gauss(double sigma, double x, double y) {
		return 1 / (2 * Math.PI * square(sigma)) * Math.exp(-0.5 / square(sigma) * (square(x) + square(
			y)));
	}

	private double square(double x) {
		return x * x;
	}

	private RandomAccessibleInterval<FloatType> create2dImage(Interval interval,
		DoubleBinaryOperator function)
	{
		RandomAccessibleInterval<Localizable> positions = Views.interval(Localizables.randomAccessible(
			2), interval);
		Converter<Localizable, FloatType> converter = (i, o) -> o.setReal(function.applyAsDouble(i
			.getDoublePosition(0), i.getDoublePosition(1)));
		return Converters.convert(positions, converter, new FloatType());
	}

	private RandomAccessible<FloatType> dirac2d() {
		RandomAccessibleInterval<FloatType> floats = ConstantUtils.constantRandomAccessibleInterval(
			new FloatType(1), new FinalInterval(1, 1));
		return Views.extendZero(floats);
	}

}
