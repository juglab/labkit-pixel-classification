
package net.imglib2.trainable_segmention.pixel_feature.filter.laplacian;

import net.imglib2.trainable_segmention.gpu.api.CLIJLoopBuilder;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.gpu.CLIJFeatureInput;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.Composite;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Plugin(type = FeatureOp.class, label = "laplacian of gaussian")
public class SingleLaplacianOfGaussianFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("laplacian of gaussian sigma=" + sigma);
	}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		int n = globalSettings().numDimensions();
		List<RandomAccessibleInterval<DoubleType>> derivatives = IntStream.range(0, n)
			.mapToObj(d -> input.derivedGauss(sigma, order(n, d))).collect(Collectors.toList());
		LoopBuilder.setImages(RevampUtils.vectorizeStack(derivatives), output.get(0))
			.multiThreaded().forEachPixel((x, sum) -> sum.setReal(sum(x, n)));
	}

	private int[] order(int n, int d) {
		return IntStream.range(0, n).map(i -> i == d ? 2 : 0).toArray();
	}

	private double sum(Composite<DoubleType> d, int n) {
		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += d.get(i).getRealDouble();
		}
		return sum;
	}

	@Override
	public void prefetch(CLIJFeatureInput input) {
		for (int d = 0; d < globalSettings().numDimensions(); d++)
			input.prefetchSecondDerivative(sigma, d, d, input.targetInterval());
	}

	@Override
	public void apply(CLIJFeatureInput input, List<GpuView> output) {
		boolean is3d = globalSettings().numDimensions() == 3;
		GpuApi gpu = input.gpuApi();
		CLIJLoopBuilder loopBuilder = CLIJLoopBuilder.gpu(gpu);
		loopBuilder.addInput("dxx", input.secondDerivative(sigma, 0, 0, input.targetInterval()));
		loopBuilder.addInput("dyy", input.secondDerivative(sigma, 1, 1, input.targetInterval()));
		if (is3d)
			loopBuilder.addInput("dzz", input.secondDerivative(sigma, 2, 2, input.targetInterval()));
		loopBuilder.addOutput("output", output.get(0));
		String operation = is3d ? "output = dxx + dyy + dzz" : "output = dxx + dyy";
		loopBuilder.forEachPixel(operation);
	}
}
