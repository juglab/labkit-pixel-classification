
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class SingleHessianEigenvaluesFeatureTest {

	public SingleHessianEigenvaluesFeatureTest(boolean useGpu) {
		this.calculator.setUseGpu(useGpu);
		this.calculator3d.setUseGpu(useGpu);
	}

	@Parameterized.Parameters(name = "useGpu = {0}")
	public static List<Boolean> data() {
		return Arrays.asList(false, true);
	}

	private final FeatureCalculator calculator = FeatureCalculator.default2d()
		.addFeature(SingleHessianEigenvaluesFeature.class)
		.build();

	private final FeatureCalculator calculator3d = FeatureCalculator.default2d()
		.dimensions(3)
		.addFeature(SingleHessianEigenvaluesFeature.class)
		.build();

	@Test
	public void testApply2d() {
		Interval interval = Intervals.createMinMax(-10, -10, 10, 10);
		RandomAccessibleInterval<FloatType> input = Utils.create2dImage(interval, (x, y) -> x * x + 2 *
			y * y + 3 * x * y);
		RandomAccessibleInterval<FloatType> output = calculator.apply(input, new FinalInterval(1, 1));
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { 6.162f,
			-0.16228f }, 1, 1, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, output, 0.001);
	}

	@Test
	public void testAttributeLabels2d() {
		List<String> attributeLabels = calculator.attributeLabels();
		List<String> expected = Arrays.asList(
			"hessian - largest eigenvalue sigma=1.0",
			"hessian - smallest eigenvalue sigma=1.0");
		assertEquals(expected, attributeLabels);
	}

	@Test
	public void testApply3d() {
		Interval interval = Intervals.createMinMax(-10, -10, -10, 10, 10, 10);
		Converter<Localizable, FloatType> converter = (position, output) -> {
			double x = position.getDoublePosition(0);
			double y = position.getDoublePosition(1);
			double z = position.getDoublePosition(2);
			output.setReal(0.5 * x * x + 2 * x * y + 3 * x * z + 2 * y * y + 5 * y * z + 3 * z * z);
		};
		// The hessian of the input image is:
		// [1 2 3]
		// [2 4 5]
		// [3 5 6]
		float eigenvalue1 = 11.345f;
		float eigenvalue2 = 0.171f;
		float eigenvalue3 = -0.516f;
		RandomAccessibleInterval<FloatType> input = Converters.convert(new Localizables()
			.randomAccessibleInterval(interval), converter, new FloatType());
		RandomAccessibleInterval<FloatType> output = calculator3d.apply(input, new FinalInterval(1, 1,
			1));
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] { eigenvalue1,
			eigenvalue2, eigenvalue3 }, 1, 1, 1, 3);
		ImgLib2Assert.assertImageEqualsRealType(output, expected, 0.001);
	}

	@Test
	public void testAttributeLabels3d() {
		List<String> attributeLabels = calculator3d.attributeLabels();
		List<String> expected = Arrays.asList(
			"hessian - largest eigenvalue sigma=1.0",
			"hessian - middle eigenvalue sigma=1.0",
			"hessian - smallest eigenvalue sigma=1.0");
		assertEquals(expected, attributeLabels);
	}
}
