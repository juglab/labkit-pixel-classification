
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * ImgLib2 version of trainable segmentation's Sobel feature.
 * 
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Sobel Gradient")
public class SingleSobelGradientFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(FeatureInput in, List<RandomAccessibleInterval<FloatType>> out) {
		calculate(in.original(), out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Sobel_filter_" + sigma);
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	private void calculate(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		double[] sigmas = { sigma * 0.4, sigma * 0.4 };

		Interval dxInputInterval = RevampUtils.deriveXRequiredInput(out);
		Interval dyInputInterval = RevampUtils.deriveYRequiredInput(out);
		Interval blurredInterval = Intervals.union(dxInputInterval, dyInputInterval);

		RandomAccessibleInterval<FloatType> blurred = RevampUtils.gauss(in, blurredInterval, sigmas);
		RandomAccessibleInterval<FloatType> dx = RevampUtils.deriveX(blurred, out);
		RandomAccessibleInterval<FloatType> dy = RevampUtils.deriveY(blurred, out);
		RandomAccessible<Pair<FloatType, FloatType>> derivatives = Views.pair(dx, dy);
		mapToFloat(derivatives, out, input -> norm2(input.getA().get(), input.getB().get()));
	}

	private <I> void mapToFloat(RandomAccessible<I> in, RandomAccessibleInterval<FloatType> out,
		ToDoubleFunction<I> operation)
	{
		Views.interval(Views.pair(in, out), out)
			.forEach(p -> p.getB().set((float) operation.applyAsDouble(p.getA())));
	}

	private static double norm2(float x, float y) {
		return Math.sqrt(x * x + y * y);
	}
}
