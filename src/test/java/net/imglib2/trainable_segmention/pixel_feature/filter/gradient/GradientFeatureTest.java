
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Collections;

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
		SingleFeatures.gradient(1.0).newInstance(Utils.ops(), GlobalSettings.default3d().build()).apply(
			in, Collections.singletonList(result));

		// test
		RandomAccessibleInterval<FloatType> expected = Utils.create3dImage(interval,
			(x, y, z) -> Math.sqrt(4 * x * x + 16 * y * y + 36 * z * z));
		Utils.assertImagesEqual(60.0, expected, result);
	}

	public static void main(String... args) {
		new GradientFeatureTest().test();
	}
}
