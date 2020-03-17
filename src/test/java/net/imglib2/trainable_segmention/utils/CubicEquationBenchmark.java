package net.imglib2.trainable_segmention.utils;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class CubicEquationBenchmark {

	private static final double[] a000 = initCoefficients(0, 0, 0);
	private static final double[] a111 = initCoefficients(1, 1, 1);
	private static final double[] a123 = initCoefficients(1, 2, 3);
	private static final double[] a223 = initCoefficients(2, 2, 3);
	private static final double[] a233 = initCoefficients(2, 3, 3);
	private static final double[] a222 = initCoefficients(2.0000001, 2.0000001, 2.0000002);
	private static final double[] aeee = initCoefficients(0.0000001, 0.00000012, 0.00000001);

	private static final double[] x = new double[3];

	@Benchmark
	public Object testSolve123() {
		return solve(a123);
	}

	@Benchmark
	public Object testSolve000() {
		return solve(a000);
	}

	@Benchmark
	public Object testSolve111() {
		return solve(a111);
	}

	@Benchmark
	public Object testSolve223() {
		return solve(a223);
	}

	@Benchmark
	public Object testSolve233() {
		return solve(a233);
	}

	@Benchmark
	public Object testSolve222() {
		return solve(a222);
	}

	@Benchmark
	public Object testSolveEEE() {
		return solve(aeee);
	}

	private Object solve(double[] a) {
		CubicEquation.solveNormalized(a[0], a[1], a[2], x);
		return x;
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(CubicEquationBenchmark.class.getSimpleName()).build();
		new Runner(options).run();
	}

	private static double[] initCoefficients(double a, double b, double c) {
		return new double[]{ - a * b * c, a * b + b * c + c * a, - a - b - c };
	}

}
