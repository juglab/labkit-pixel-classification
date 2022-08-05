/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.gpu.random_forest;

import com.google.gson.JsonElement;
import hr.irb.fastRandomForest.FastRandomForest;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.classification.Segmenter;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuApi;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuImage;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPool;
import sc.fiji.labkit.pixel_classification.gson.GsonUtils;
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
	private static final GpuRandomForestPrediction prediction = new GpuRandomForestPrediction(
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
