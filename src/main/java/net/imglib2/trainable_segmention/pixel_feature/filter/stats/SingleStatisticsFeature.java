
package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import clij.CLIJLoopBuilder;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJFeatureInput;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.algorithm.convolution.Convolution;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;

@Plugin(type = FeatureOp.class, label = "statistic filters")
public class SingleStatisticsFeature extends AbstractFeatureOp {

	@Parameter
	private boolean min = true;

	@Parameter
	private boolean max = true;

	@Parameter
	private boolean mean = true;

	@Parameter
	private boolean variance = true;

	@Parameter
	private double radius = 1;

	@Override
	public int count() {
		return asInt(min) + asInt(max) + asInt(mean) + asInt(variance);
	}

	private int asInt(boolean value) {
		return value ? 1 : 0;
	}

	@Override
	public List<String> attributeLabels() {
		List<String> attributes = new ArrayList<>();
		if (min) attributes.add("min filter radius=" + radius);
		if (max) attributes.add("max filter radius=" + radius);
		if (mean) attributes.add("mean filter radius=" + radius);
		if (variance) attributes.add("variance filter radius=" + radius);
		return attributes;
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		if (radius <= 0)
			applyRadiusZero(input, output);
		else
			applyRadiusGreaterThanZero(input, output);
	}

	private void applyRadiusZero(FeatureInput input,
		List<RandomAccessibleInterval<FloatType>> output)
	{
		Iterator<RandomAccessibleInterval<FloatType>> o = output.iterator();
		if (min) RealTypeConverters.copyFromTo(input.original(), o.next());
		if (max) RealTypeConverters.copyFromTo(input.original(), o.next());
		if (mean) RealTypeConverters.copyFromTo(input.original(), o.next());
		if (variance) LoopBuilder.setImages(o.next()).forEachPixel(FloatType::setZero);
	}

	private void applyRadiusGreaterThanZero(FeatureInput input,
		List<RandomAccessibleInterval<FloatType>> output)
	{
		Iterator<RandomAccessibleInterval<FloatType>> o = output.iterator();
		int[] windowSize = globalSettings().pixelSize().stream().mapToInt(pixelSize -> 1 + 2 *
			(int) (radius / pixelSize)).toArray();
		if (min) MinMaxFilter.minFilter(windowSize).process(input.original(), o.next());
		if (max) MinMaxFilter.maxFilter(windowSize).process(input.original(), o.next());
		calculateMeanAndVariance(windowSize, input, o);
	}

	private void calculateMeanAndVariance(int[] windowSize, FeatureInput input,
		Iterator<RandomAccessibleInterval<FloatType>> o)
	{
		if (!mean && !variance)
			return;
		RandomAccessibleInterval<FloatType> meanBuffer = mean ? o.next() : null;
		RandomAccessibleInterval<FloatType> varianceBuffer = variance ? o.next() : null;
		if (!mean) {
			long[] size = Intervals.dimensionsAsLongArray(varianceBuffer);
			long[] min = Intervals.minAsLongArray(varianceBuffer);
			meanBuffer = Views.translate(ArrayImgs.floats(size), min);
		}
		calculateMean(windowSize, input, meanBuffer);
		if (variance)
			calculateVariance(windowSize, input, meanBuffer, varianceBuffer);
	}

	static void calculateMean(int[] windowSize, FeatureInput input,
		RandomAccessibleInterval<FloatType> output)
	{
		Convolution<RealType<?>> sumFilter = SumFilter.convolution(windowSize);
		sumFilter.process(input.original(), output);
		double factor = 1.0 / Intervals.numElements(windowSize);
		LoopBuilder.setImages(output).forEachPixel(pixel -> pixel.mul(factor));
	}

	private void calculateVariance(int[] windowSize, FeatureInput input,
		RandomAccessibleInterval<FloatType> mean, RandomAccessibleInterval<FloatType> output)
	{
		RandomAccessible<FloatType> original = input.original();
		RandomAccessible<FloatType> squared = Converters.convert(original, (i, o) -> o.set(square(i
			.getRealFloat())), new FloatType());
		Convolution<RealType<?>> sumFilter = SumFilter.convolution(windowSize);
		sumFilter.process(squared, output);
		long n = Intervals.numElements(windowSize);
		float a = 1.0f / (n - 1);
		float b = (float) n / (n - 1);
		LoopBuilder.setImages(output, mean).forEachPixel((o, m) -> o.setReal(o.getRealFloat() * a -
			square(m.getRealFloat()) * b));
	}

	private float square(float value) {
		return value * value;
	}

	@Override
	public void prefetch(CLIJFeatureInput input) {
		long[] border = globalSettings().pixelSize().stream()
				.mapToLong(pixelSize -> (long) (radius / pixelSize)).toArray();
		input.prefetchOriginal(Intervals.expand(input.targetInterval(), border));
	}

	@Override
	public void apply(CLIJFeatureInput input, List<CLIJView> output) {
		CLIJ2 clij = input.clij();
		long[] border = globalSettings().pixelSize().stream().mapToLong(pixelSize -> (long) (radius / pixelSize)).toArray();
		Iterator<CLIJView> iterator = output.iterator();
		Interval interval = input.targetInterval();
		CLIJView original = input.original(Intervals.expand(interval, border));
		try(
				ClearCLBuffer inputBuffer = copyView(clij, original);
				ClearCLBuffer tmp = clij.create(inputBuffer);
		)
		{
			if(min) {
				min(clij, inputBuffer, tmp, border);
				CLIJCopy.copy(clij, CLIJView.shrink(tmp, border), iterator.next());
			}
			if(max) {
				max(clij, inputBuffer, tmp, border);
				CLIJCopy.copy(clij, CLIJView.shrink(tmp, border), iterator.next());
			}
			calculateMeanAndVariance(clij, border, iterator, inputBuffer, tmp);
		}
	}

	private void min(CLIJ2 clij, ClearCLBuffer inputBuffer, ClearCLBuffer tmp, long[] border) {
		if (border.length == 2)
			clij.minimum2DBox(inputBuffer, tmp, border[0], border[1]);
		else
			clij.minimum3DBox(inputBuffer, tmp, border[0], border[1], border[2]);
	}

	private void max(CLIJ2 clij, ClearCLBuffer input, ClearCLBuffer output, long[] border) {
		if (border.length == 2)
			clij.maximum2DBox(input, output, border[0], border[1]);
		else
			clij.maximum3DBox(input, output, border[0], border[1], border[2]);
	}

	private void calculateMeanAndVariance(CLIJ2 clij, long[] border, Iterator<CLIJView> iterator, ClearCLBuffer inputBuffer, ClearCLBuffer tmp) {
		if(!mean && !variance)
			return;
		mean(clij, inputBuffer, tmp, border);
		if (mean)
			CLIJCopy.copy(clij, CLIJView.shrink(tmp, border), iterator.next());
		if (variance) {
			try (
					ClearCLBuffer squared = clij.create(inputBuffer);
					ClearCLBuffer meanOfSquared = clij.create(inputBuffer);
			) {
				square(clij, inputBuffer, squared);
				mean(clij, squared, meanOfSquared, border);
				long n = LongStream.of(border).map(b -> 2 * b + 1).reduce(1, (a, b) -> a * b);
				CLIJLoopBuilder.clij(clij)
						.addInput("mean", CLIJView.shrink(tmp, border))
						.addInput("mean_of_squared", CLIJView.shrink(meanOfSquared, border))
						.addInput("factor", (float) n / (n - 1))
						.addOutput("o", iterator.next())
						.forEachPixel("o = (mean_of_squared - mean * mean) * factor");
			}
		}
	}

	private void mean(CLIJ2 clij, ClearCLBuffer input, ClearCLBuffer output, long[] border) {
		if(border.length == 2)
			clij.mean2DBox(input, output, border[0], border[1]);
		else
			clij.mean3DBox(input, output, border[0], border[1], border[2]);
	}

	private void square(CLIJ2 clij, ClearCLBuffer inputBuffer, ClearCLBuffer tmp2) {
		CLIJLoopBuilder.clij(clij).addInput("a", inputBuffer).addOutput("b", tmp2)
				.forEachPixel("b = a * a");
	}
	private ClearCLBuffer copyView(CLIJ2 clij, CLIJView inputClBuffer) {
		ClearCLBuffer buffer = clij.create(Intervals.dimensionsAsLongArray(inputClBuffer.interval()));
		CLIJCopy.copy(clij, inputClBuffer, CLIJView.wrap(buffer));
		return buffer;
	}
}
