
package net.imglib2.trainable_segmention.gpu.random_forest;

import com.google.gson.JsonElement;
import hr.irb.fastRandomForest.FastRandomForest;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.gpu.GpuFeatureInput;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuCopy;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuPool;
import net.imglib2.trainable_segmention.gpu.api.GpuViews;
import net.imglib2.trainable_segmention.gson.GsonUtils;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
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
import weka.classifiers.Classifier;

@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
public class GpuRandomForestKernelBenchmark {

	private static final int numberOfFeatures = 10;
	private static final int numberOfClasses = 2;

	private static final GpuApi gpu = GpuPool.borrowGpu();
	private static final RandomForestPrediction prediction = initializeRandomForest();
	private static final GpuImage features = initializeFeatures(gpu);
	private static final GpuImage distribution = gpu.create(features.getDimensions(), numberOfClasses, NativeTypeEnum.Float);

	@TearDown
	public void tearDown() {
		gpu.close();
	}

	@Benchmark
	public Object benchmark() {
		prediction.distribution(gpu, features, distribution);
		return distribution;
	}

	private static RandomForestPrediction initializeRandomForest() {
		Context context = new Context();
		JsonElement read = GsonUtils.read(GpuRandomForestKernelBenchmark.class.getResource("/clij/test.classifier") .getFile());
		Segmenter segmenter = Segmenter.fromJson(context, read);
		Classifier classifier = segmenter.getClassifier();
		return new RandomForestPrediction((FastRandomForest) classifier, numberOfClasses, numberOfFeatures);
	}

	private static GpuImage initializeFeatures(GpuApi gpu) {
		RandomAccessibleInterval<FloatType> input = Utils.loadImageFloatType("/home/arzt/Documents/Datasets/Example/small-3d-stack.tif");
		return calculateFeatures(gpu, input);
	}

	private static GpuImage calculateFeatures(GpuApi gpu, RandomAccessibleInterval<FloatType> input) {
		GpuImage output = gpu.create(Intervals.dimensionsAsLongArray(input), numberOfFeatures, NativeTypeEnum.Float);
		for (int i = 0; i < numberOfFeatures; i++) {
			try (GpuApi scope = gpu.subScope()) {
				GpuFeatureInput fi = new GpuFeatureInput(scope, Views.extendBorder(input), input, new double[]{1, 1, 1});
				float sigma = i * 2;
				fi.prefetchGauss(sigma, input);
				GpuCopy.copyFromTo(scope, fi.gauss(sigma, input), GpuViews.channel(output, i));
			}
		}
		return output;
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(GpuRandomForestKernelBenchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}

}
