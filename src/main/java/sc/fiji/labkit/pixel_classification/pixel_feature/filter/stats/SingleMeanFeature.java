package sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.gpu.algorithms.GpuNeighborhoodOperations;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.algorithm.convolution.Convolution;
import preview.net.imglib2.converter.RealTypeConverters;
import preview.net.imglib2.loops.LoopBuilder;

@Plugin(type = FeatureOp.class, label = "mean filter")
public class SingleMeanFeature extends AbstractSingleStatisticFeature {

	@Override
	protected String filterName() {
		return "mean";
	}

	@Override
	protected void apply(int[] windowSize, RandomAccessible<FloatType> input, RandomAccessibleInterval<FloatType> output) {
		if(Intervals.numElements(windowSize) <= 1)
			RealTypeConverters.copyFromTo(input, output);
		else {
			Convolution<RealType<?>> sumFilter = SumFilter.convolution(windowSize);
			sumFilter.process(input, output);
			double factor = 1.0 / Intervals.numElements(windowSize);
			LoopBuilder.setImages(output).forEachPixel(pixel -> pixel.mul(factor));
		}
	}

	@Override
	protected void apply(GpuApi gpu, int[] windowSize, GpuView input, GpuView output) {
		GpuNeighborhoodOperations.mean(gpu, windowSize).apply(input, output);
	}

}
