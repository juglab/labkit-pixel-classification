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

package sc.fiji.labkit.pixel_classification.performance;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.loops.LoopBuilder;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;

public class GpuCpuComparisonBenchmark {

	public static void main(String... args) {
		ParallelSegmentationTask task = new ParallelSegmentationTask();
		// Gpu
		task.setUseGpu(true);
		task.run();
		task.printTimes();
		Img<UnsignedShortType> gpuResult = task.getSegmenation();
		long nanosecondsGpu = task.measuredTime().nanoTime();

		// Cpu
		task.setUseGpu(false);
		task.run();
		task.printTimes();
		Img<UnsignedShortType> cpuResult = task.getSegmenation();
		long nanosecondsCpu = task.measuredTime().nanoTime();

		System.out.println("Speed up: " + (float) nanosecondsCpu / nanosecondsGpu);
		long differences = countDifferentPixels(gpuResult, cpuResult);
		assertTrue(differences < Intervals.numElements(cpuResult) / 10000);
	}

	public static long countDifferentPixels(Img<UnsignedShortType> gpuResult,
		Img<UnsignedShortType> cpuResult)
	{
		AtomicLong counter = new AtomicLong(0);
		LoopBuilder.setImages(gpuResult, cpuResult).forEachPixel((a, b) -> {
			if (!a.equals(b)) counter.incrementAndGet();
		});
		return counter.get();
	}
}
