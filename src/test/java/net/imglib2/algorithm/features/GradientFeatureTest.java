package net.imglib2.algorithm.features;

import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by arzt on 18.07.17.
 */
public class GradientFeatureTest {

	@Test
	public void test() {
		// setup
		OpService ops = RevampUtils.ops();

		Interval interval = new FinalInterval(new long[]{0, 0, 0}, new long[]{20, 20, 20});
		Interval biggerInterval = Intervals.expand(interval, new long[]{10, 10, 10});

		Img<FloatType> in = ops.create().img(biggerInterval, new FloatType());
		ops.image().equation(in, "p[0]*p[0] + 2*p[1]*p[1] + 3*p[2]*p[2]");

		// process
		Img<FloatType> result = ops.create().img(interval, new FloatType());
		SingleFeatures.gradient(0.0).apply(in, Collections.singletonList(result));

		// test
		Img<FloatType> expected = ops.create().img(interval, new FloatType());
		ops.image().equation(expected, "Math.sqrt(4 * p[0]*p[0] + 16 * p[1]*p[1] + 36 * p[2]*p[2])");
		Utils.assertImagesEqual(100.0, expected, result);
	}

	public static void main(String... args) {
		new GradientFeatureTest().test();
	}
}
