package net.imglib2.trainable_segmention.pixel_feature.filter;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.DerivedNormalDistribution;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureInput {

	private final RandomAccessible<FloatType> original;
	private final Interval target;
	private final Map<Double, RandomAccessibleInterval<DoubleType>> gaussCache
			= new ConcurrentHashMap<>();
	private final Map<Object, RandomAccessibleInterval<DoubleType>> derivatives
			= new ConcurrentHashMap<>();

	public FeatureInput(RandomAccessible<FloatType> original, Interval targetInterval) {
		this.original = original;
		this.target = new FinalInterval(targetInterval);
	}

	public RandomAccessible<FloatType> original() {
		return original;
	}

	public Interval targetInterval() {
		return target;
	}

	public RandomAccessibleInterval<? extends RealType<?>> gauss(double sigma) {
		return gaussCache.computeIfAbsent(sigma, this::calculateGauss);
	}

	private RandomAccessibleInterval<DoubleType> calculateGauss(double sigma) {
		final RandomAccessibleInterval<DoubleType> result = create(target);
		Gauss3.gauss(sigma, (RandomAccessible) original, result);
		return result;
	}

	public RandomAccessibleInterval<DoubleType> derivedGauss(double sigma, int... order) {
		return derivatives.computeIfAbsent(key(sigma, order), k -> calculateDerivative(sigma, order));
	}

	private static Object key(double sigma, int... order) {
		return Arrays.asList(sigma, new TIntArrayList(order));
	}

	private RandomAccessibleInterval<DoubleType> calculateDerivative(double sigma, int[] order) {
		Kernel1D[] kernels = Arrays.stream(order)
				.mapToObj(o -> DerivedNormalDistribution.derivedGaussKernel(sigma, o))
				.toArray(Kernel1D[]::new);
		final RandomAccessibleInterval<DoubleType> result = create(target);
		SeparableKernelConvolution.convolution(kernels).process(original, result);
		return result;
	}

	private RandomAccessibleInterval<DoubleType> create(Interval target) {
		return Views.translate(ArrayImgs.doubles(Intervals.dimensionsAsLongArray(target)), Intervals.minAsLongArray(target));
	}
}
