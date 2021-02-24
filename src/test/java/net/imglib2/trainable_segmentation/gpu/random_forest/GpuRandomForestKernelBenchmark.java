
package net.imglib2.trainable_segmentation.gpu.random_forest;

import com.google.gson.JsonElement;
import hr.irb.fastRandomForest.FastRandomForest;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmentation.Utils;
import net.imglib2.trainable_segmentation.classification.Segmenter;
import net.imglib2.trainable_segmentation.gpu.api.GpuApi;
import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.imglib2.trainable_segmentation.gpu.api.GpuPool;
import net.imglib2.trainable_segmentation.gson.GsonUtils;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.scijava.Context;

@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
public class GpuRandomForestKernelBenchmark {

	private static final int numberOfFeatures = 10;
	private static final int numberOfClasses = 2;

	private static final GpuApi gpu = GpuPool.borrowGpu();
	private static final Segmenter segmenter = initializeSegmenter();
	private static final GpuRandomForestPrediction
			prediction = new GpuRandomForestPrediction(
		(FastRandomForest) segmenter.getClassifier(), numberOfFeatures);
	private static final GpuImage features = initializeFeatures(gpu);
	private static final GpuImage distribution = gpu.create(features.getDimensions(), numberOfClasses,
		NativeTypeEnum.Float);

	@TearDown
	public void tearDown() {
		gpu.close();
	}

	@Benchmark
	public Object benchmark() {
		prediction.distribution(gpu, features, distribution);
		return distribution;
	}

	private static Segmenter initializeSegmenter() {
		Context context = new Context();
		JsonElement read = GsonUtils.read(GpuRandomForestKernelBenchmark.class.getResource(
			"/clij/t1-head.classifier").getFile());
		return Segmenter.fromJson(context, read);
	}

	private static GpuImage initializeFeatures(GpuApi gpu) {
		RandomAccessibleInterval<FloatType> input = Utils.loadImageFloatType(
			"https://imagej.net/images/t1-head.zip");
		return segmenter.features().applyUseGpu(gpu, Views.extendBorder(input), new FinalInterval(100,
			100, 100));
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GpuRandomForestKernelBenchmark.class
			.getSimpleName()).build();
		new Runner(options).run();
	}

}
