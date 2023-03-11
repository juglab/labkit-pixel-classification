/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labkit.pixel_classification.classification.Segmenter;
import sc.fiji.labkit.pixel_classification.classification.Trainer;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.GroupedFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.StopWatch;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.scijava.Context;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.Parallelization;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 15, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(0)
public class SegmentationBenchmark {

	private static final String imageFilename =
		"drosophila_3d.tif";

	private static final String labelingFileName =
		"drosophila_3d_labeling.tif";

	private final Context context = SingletonContext.getInstance();

	private final RandomAccessibleInterval<FloatType> image = Utils.loadImageFloatType(imageFilename);

	private final LabelRegions<?> labelRegions = asLabelRegions(Utils.loadImageFloatType(
		labelingFileName));

	private final Segmenter segmenter = trainSegmenter();

	private Segmenter trainSegmenter() {
		final FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default3d().build(),
			GroupedFeatures.gauss(),
			GroupedFeatures.differenceOfGaussians(),
			GroupedFeatures.hessian(),
			GroupedFeatures.gradient());
		return Parallelization.runSingleThreaded(() -> Trainer.train(context, image, labelRegions,
			featureSettings));
	}

	@Benchmark
	public Object benchmarkSegment() {
		return Parallelization.runSingleThreaded(() -> segmenter.segment(image));
	}

	@Benchmark
	public Segmenter benchmarkTrain() {
		return trainSegmenter();
	}

	public static void main(String... args) throws RunnerException {
		runBenchmark();
	}

	private static void runForever() {
		StopWatch stopWatch = StopWatch.createAndStart();
		SegmentationBenchmark segmentationBenchmark = new SegmentationBenchmark();
		while (true) {
			System.out.println("start " + stopWatch);
			segmentationBenchmark.benchmarkSegment();
		}
	}

	private static void runBenchmark() throws RunnerException {
		Options options = new OptionsBuilder().include(SegmentationBenchmark.class.getSimpleName())
			.build();
		new Runner(options).run();
	}

	// -- Helper --

	private LabelRegions asLabelRegions(RandomAccessibleInterval<? extends RealType<?>> labeling) {
		Img<UnsignedByteType> ints = ArrayImgs.unsignedBytes(Intervals.dimensionsAsLongArray(labeling));
		RandomAccessibleInterval<LabelingType<String>> labelingTypes = new ImgLabeling<>(ints);
		LoopBuilder.setImages(labeling, labelingTypes).multiThreaded().forEachPixel((i, o) -> {
			if (i.getRealFloat() != 0) o.add(i.toString());
		});
		return new LabelRegions<>(labelingTypes);
	}

}
