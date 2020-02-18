
package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.StackProcessor;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.test.RandomImgs;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.StopWatch;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.scijava.Context;

import java.util.Collections;

/**
 * Benchmark that compares the performance of max filters in IJ1, ops and
 * {@link MinMaxFilter}.
 */
@Fork(1)
@State(Scope.Benchmark)
@Warmup(iterations = 4)
@Measurement(iterations = 4)
@BenchmarkMode(Mode.AverageTime)
public class MaxFilterBenchmark {

	private OpService ops = new Context().service(OpService.class);
	private ImageStack inputStack = randomize(ImageStack.create(100, 100, 100, 32));
	private ImageStack outputStack = ImageStack.create(100, 100, 100, 32);

	private Img<FloatType> input = RandomImgs.seed(42).nextImage(new FloatType(), 100, 100, 100);
	private Img<FloatType> output = ArrayImgs.floats(100, 100, 100);

	private final int radius = 3;

	private ImageStack randomize(ImageStack stack) {
		RandomImgs.seed(42).randomize(ImageJFunctions.wrapFloat(new ImagePlus("", stack)));
		return stack;
	}

	@Benchmark
	public void benchMarkIJ1() {
		StackProcessor processor = new StackProcessor(inputStack);
		processor.filter3D(outputStack, radius, radius, radius, 1, 100, StackProcessor.FILTER_MAX);
	}

	@Benchmark
	public void benchmarkMaxFilter() {
		int width = 3 * radius + 1;
		MinMaxFilter.maxFilter(width, width, width).process(Views.extendBorder(input), output);
	}

	@Deprecated
	@Benchmark
	public void benchmarkOps() {
		FeatureOp feature = new FeatureSetting(SingleSphereShapedFeature.class, "radius", 4,
			"operation", SingleSphereShapedFeature.MAX)
				.newInstance(ops, GlobalSettings.default3d().build());
		feature.apply(Views.extendBorder(input), Collections.singletonList(output));
	}

	// @Setup
	public void warmup() {
		for (int i = 0; i < 2; i++) {
			maxFilter(new IntType());
			maxFilter(new DoubleType());
			maxFilter(new FloatType());
			maxFilter(new UnsignedByteType());
		}
	}

	private <T extends RealType<T> & NativeType<T>> void maxFilter(T type) {
		Img<T> input = RandomImgs.seed(42).nextImage(type, 100, 100, 100);
		Img<T> output = new ArrayImgFactory<>(type).create(100, 100, 100);
		StopWatch stopWatch = StopWatch.createAndStart();
		int width = 3 * radius + 1;
		MinMaxFilter.maxFilter(width, width, width).process(Views.extendBorder(input), output);
		System.out.println(type.getClass().getSimpleName() + ": " + stopWatch);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(MaxFilterBenchmark.class.getSimpleName())
			.build();
		new Runner(options).run();
	}

}
