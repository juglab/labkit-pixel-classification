
package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import net.imglib2.trainable_segmention.gpu.algorithms.GpuNeighborhoodOperation;
import net.imglib2.trainable_segmention.gpu.algorithms.GpuNeighborhoodOperations;
import net.imglib2.trainable_segmention.gpu.api.GpuPixelWiseOperation;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.gpu.api.GpuCopy;
import net.imglib2.trainable_segmention.gpu.GpuFeatureInput;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
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
import java.util.stream.IntStream;
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
	public void prefetch(GpuFeatureInput input) {
		long[] border = globalSettings().pixelSize().stream()
			.mapToLong(pixelSize -> (long) (radius / pixelSize)).toArray();
		input.prefetchOriginal(Intervals.expand(input.targetInterval(), border));
	}

	@Override
	public void apply(GpuFeatureInput input, List<GpuView> output) {
		try (GpuApi gpu = input.gpuApi().subScope()) {
			long[] border = globalSettings().pixelSize().stream().mapToLong(pixelSize -> (long) (radius /
				pixelSize)).toArray();
			Iterator<GpuView> iterator = output.iterator();
			Interval interval = input.targetInterval();
			GpuView original = input.original(Intervals.expand(interval, border));
			int[] windowSize = LongStream.of(border).mapToInt(x -> (int) (1 + 2 * x)).toArray();
			if (min) {
				GpuNeighborhoodOperations.min(gpu, windowSize).apply(original, iterator.next());
			}
			if (max) {
				GpuNeighborhoodOperations.max(gpu, windowSize).apply(original, iterator.next());
			}
			calculateMeanAndVariance(gpu, windowSize, original, iterator);
		}
	}

	private void calculateMeanAndVariance(GpuApi gpu, int[] windowSize, GpuView original,
		Iterator<GpuView> iterator)
	{
		if (!mean && !variance)
			return;
		if (mean) {
			GpuView mean = iterator.next();
			GpuNeighborhoodOperations.mean(gpu, windowSize).apply(original, mean);
			if (variance)
				calculateVariance(gpu, windowSize, original, mean, iterator.next());
		}
		else {
			GpuView variance = iterator.next();
			GpuImage mean = gpu.create(Intervals.dimensionsAsLongArray(variance.dimensions()),
				NativeTypeEnum.Float);
			GpuNeighborhoodOperations.mean(gpu, windowSize).apply(original, GpuViews.wrap(mean));
			calculateVariance(gpu, windowSize, original, GpuViews.wrap(mean), variance);
		}
	}

	private void calculateVariance(GpuApi gpu, int[] windowSize, GpuView original, GpuView mean,
		GpuView variance)
	{
		GpuImage squared = gpu.create(Intervals.dimensionsAsLongArray(original.dimensions()),
			NativeTypeEnum.Float);
		GpuImage meanOfSquared = gpu.create(Intervals.dimensionsAsLongArray(variance.dimensions()),
			NativeTypeEnum.Float);
		long n = Intervals.numElements(windowSize);
		if (n <= 1)
			GpuPixelWiseOperation.gpu(gpu).addOutput("variance", variance).forEachPixel("variance = 0");
		else {
			square(gpu, original, squared);
			GpuNeighborhoodOperations.mean(gpu, windowSize).apply(GpuViews.wrap(squared), GpuViews.wrap(
				meanOfSquared));
			GpuPixelWiseOperation.gpu(gpu)
				.addInput("mean", mean)
				.addInput("mean_of_squared", meanOfSquared)
				.addInput("factor", (float) n / (n - 1))
				.addOutput("variance", variance)
				.forEachPixel("variance = (mean_of_squared - mean * mean) * factor");
		}
	}

	private void square(GpuApi gpu, GpuView inputBuffer, GpuImage tmp2) {
		GpuPixelWiseOperation.gpu(gpu).addInput("a", inputBuffer).addOutput("b", tmp2)
			.forEachPixel("b = a * a");
	}
}
