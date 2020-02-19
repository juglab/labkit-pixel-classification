
package net.imglib2.trainable_segmention;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Benchmarks {

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder()
			.include(SegmentationBenchmark.class.getSimpleName())
			.include(FeatureCalculationBenchmark.class.getSimpleName())
			.build();
		new Runner(options).run();
	}
}
