
package clij;

import ij.ImagePlus;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJMultiChannelImage;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CLIJFeatureCalculatorTest {

	static {
		LegacyInjector.preinit();
	}

	private CLIJ2 clij;

	@Before
	public void before() {
		clij = CLIJ2.getInstance();
	}

	@After
	public void after() {
		clij.clear();
	}

	private ImgPlus<FloatType> input = VirtualStackAdapter.wrapFloat(
		new ImagePlus("/home/arzt/Documents/Datasets/Example/small-3d-stack.tif"));

	@Test
	public void testSingleGauss() {
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.dimensions(3)
			.addFeature(SingleFeatures.gauss(8))
			.build();
		FinalInterval interval = Intervals.expand(input, -40);
		try (CLIJMultiChannelImage featureStack = calculator.applyWithCLIJ(Views.extendBorder(input),
			interval))
		{
			RandomAccessibleInterval<FloatType> result = featureStack.asRAI();
			RandomAccessibleInterval<FloatType> expected = calculator.apply(Views.extendBorder(input),
				interval);
			Utils.assertImagesEqual(50, expected, result);
		}
	}

	@Test
	public void testTwoGaussians() {
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.dimensions(3)
			.addFeature(SingleFeatures.gauss(1))
			.addFeature(SingleFeatures.gauss(2))
			.build();
		FinalInterval interval = Intervals.expand(input, -40);
		try (CLIJMultiChannelImage featureStack = calculator.applyWithCLIJ(Views.extendBorder(input),
			interval))
		{
			RandomAccessibleInterval<FloatType> result = featureStack.asRAI();
			RandomAccessibleInterval<FloatType> expected = calculator.apply(Views.extendBorder(input),
				interval);
			Utils.assertImagesEqual(50, expected, result);
		}
	}
}
