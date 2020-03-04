package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

public class CLIJFeatureInputTest {


	private final RandomAccessible<FloatType> original = Views.extendBorder(RandomImgs.seed(42).nextImage(new FloatType(), 10, 10, 10));
	private final Interval targetInterval = Intervals.createMinMax(5, 5, 5, 9, 9, 9);
	private final double[] pixelSize = new double[]{1, 1, 1};
	private final CLIJ2 clij = CLIJ2.getInstance();

	@Test
	public void test() {
		try( CLIJFeatureInput featureInput = new CLIJFeatureInput(clij, original, targetInterval, pixelSize) ) {
			FinalInterval interval = Intervals.expand(targetInterval, 3);
			featureInput.prefetchOriginal(interval);
			CLIJView result = featureInput.original(interval);
			RandomAccessibleInterval<FloatType> cutout = Views.translate(clij.pullRAI(result.buffer()), Intervals.minAsLongArray(interval));
			ImgLib2Assert.assertImageEquals(Views.interval(original, interval), cutout);
		}
	}
}
