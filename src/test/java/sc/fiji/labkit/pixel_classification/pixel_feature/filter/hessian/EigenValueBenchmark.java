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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter.hessian;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.linalg.eigen.EigenValues;
import net.imglib2.algorithm.linalg.eigen.EigenValuesSymmetric;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.RandomImgs;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.composite.Composite;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import net.imglib2.loops.LoopBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Fork(1)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class EigenValueBenchmark {

	private final long dims[] = { 50, 50, 50 };

	private final List<RandomAccessibleInterval<DoubleType>> slices = IntStream.range(0, 6)
		.mapToObj(i -> RandomImgs.seed(i).nextImage(new DoubleType(), dims))
		.collect(Collectors.toList());

	private final List<RandomAccessibleInterval<DoubleType>> eigenvalues = IntStream.range(0, 6)
		.mapToObj(i -> ArrayImgs.doubles(dims))
		.collect(Collectors.toList());

	@Benchmark
	public Object benchmarkImgLib2AlgorithmEigenValuesSymmetric3D() {
		RandomAccessibleInterval<Composite<DoubleType>> matrix = RevampUtils.vectorizeStack(slices);
		RandomAccessibleInterval<Composite<DoubleType>> result = RevampUtils.vectorizeStack(
			eigenvalues);
		EigenValuesSymmetric<DoubleType, DoubleType> calculator = EigenValues.symmetric(3);
		LoopBuilder.setImages(matrix, result).forEachPixel(calculator::compute);
		return eigenvalues;
	}

	@Benchmark
	public Object benchmarkEigenValuesSymmetric3D() {
		RandomAccessibleInterval<Composite<DoubleType>> matrix = RevampUtils.vectorizeStack(slices);
		RandomAccessibleInterval<Composite<DoubleType>> result = RevampUtils.vectorizeStack(
			eigenvalues);
		EigenValues<DoubleType, DoubleType> calculator = new EigenValuesSymmetric3D<>();
		LoopBuilder.setImages(matrix, result).forEachPixel(calculator::compute);
		return eigenvalues;
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(EigenValueBenchmark.class.getSimpleName())
			.build();
		new Runner(options).run();
	}
}
