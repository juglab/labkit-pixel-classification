
package sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.gradient;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

/**
 * Created by arzt on 18.07.17.
 */
@Deprecated
public class GradientFeatureTest {

	@Test
	public void test() {
		Interval interval = new FinalInterval(new long[] { 0, 0, 0 }, new long[] { 20, 20, 20 });
		Interval biggerInterval = Intervals.expand(interval, new long[] { 10, 10, 10 });

		RandomAccessibleInterval<FloatType> in = Utils.create3dImage(biggerInterval,
			(x, y, z) -> x * x + 2 * y * y + 3 * z * z);

		// process
		RandomAccessibleInterval<FloatType> result = RevampUtils.createImage(interval, new FloatType());
		FeatureCalculator calculator = FeatureCalculator.default2d()
			.dimensions(3)
			.addFeature(SingleFeatures.gradient(1))
			.build();
		calculator.apply(in, Views.addDimension(result, 0, 0));

		// test
		RandomAccessibleInterval<FloatType> expected = Utils.create3dImage(interval,
			(x, y, z) -> Math.sqrt(4 * x * x + 16 * y * y + 36 * z * z));
		Utils.assertImagesEqual(60.0, expected, result);
	}
}
