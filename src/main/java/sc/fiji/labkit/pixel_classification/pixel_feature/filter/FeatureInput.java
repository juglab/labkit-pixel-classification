/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.pixel_feature.filter;

import gnu.trove.list.array.TIntArrayList;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.converter.RealTypeConverters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.DoubleStream;

public class FeatureInput {

	private final RandomAccessible<FloatType> original;
	private final Interval target;
	private final Map<Double, RandomAccessibleInterval<DoubleType>> gaussCache =
		new ConcurrentHashMap<>();
	private final Map<Object, RandomAccessibleInterval<DoubleType>> derivatives =
		new ConcurrentHashMap<>();
	private double[] pixelSize;

	/**
	 * Expected channel order XY and optional Z.
	 */
	public FeatureInput(RandomAccessible<FloatType> original, Interval targetInterval,
		double[] pixelSize)
	{
		this.original = original;
		this.pixelSize = pixelSize;
		this.target = new FinalInterval(targetInterval);
	}

	/**
	 * This method set's the pixel size, the sigma for gauss-filter and derivatives
	 * are scaled accordingly.
	 */
	public void setPixelSize(double... pixelSize) {
		this.pixelSize = pixelSize;
	}

	public RandomAccessible<FloatType> original() {
		return original;
	}

	public Interval targetInterval() {
		return target;
	}

	public RandomAccessibleInterval<DoubleType> gauss(double sigma) {
		return Views.interval(extendedGauss(sigma), target);
	}

	private RandomAccessibleInterval<DoubleType> extendedGauss(double sigma) {
		return gaussCache.computeIfAbsent(sigma, this::calculateGauss);
	}

	private RandomAccessibleInterval<DoubleType> calculateGauss(double sigma) {
		final RandomAccessibleInterval<DoubleType> result = create(Intervals.expand(target, 2));
		if (sigma == 0)
			RealTypeConverters.copyFromTo(original, result);
		else
			Gauss3.gauss(scaledSigmas(sigma), (RandomAccessible) original, result);
		return result;
	}

	private double[] scaledSigmas(double sigma) {
		return DoubleStream.of(pixelSize).map(p -> sigma / p).toArray();
	}

	public RandomAccessibleInterval<DoubleType> derivedGauss(double sigma, int... order) {
		return derivatives.computeIfAbsent(key(sigma, order), k -> calculateDerivative(sigma, order));
	}

	private static Object key(double sigma, int... order) {
		return Arrays.asList(sigma, new TIntArrayList(order));
	}

	private RandomAccessibleInterval<DoubleType> calculateDerivative(double sigma, int[] orders) {
		List<Convolution<NumericType<?>>> convolutions = new ArrayList<>();
		for (int i = 0; i < orders.length; i++) {
			int order = orders[i];
			if (order != 0) {
				Kernel1D multiply = multiply(SIMPLE_KERNELS.get(order), Math.pow(pixelSize[i], -order));
				convolutions.add(SeparableKernelConvolution.convolution1d(multiply, i));
			}
		}
		if (convolutions.isEmpty())
			return gauss(sigma);
		final RandomAccessibleInterval<DoubleType> result = create(target);
		Convolution.concat(convolutions).process(extendedGauss(sigma), result);
		return result;
	}

	static List<Kernel1D> SIMPLE_KERNELS = Arrays.asList(
		Kernel1D.centralAsymmetric(1),
		Kernel1D.centralAsymmetric(0.5, 0, -0.5),
		Kernel1D.centralAsymmetric(1, -2, 1));

	private Kernel1D multiply(Kernel1D kernel1D, double scaleFactor) {
		double[] fullKernel = multiply(kernel1D.fullKernel(), scaleFactor);
		int originIndex = (int) -kernel1D.min();
		return Kernel1D.asymmetric(fullKernel, originIndex);
	}

	private double[] multiply(double[] array, double scaleFactor) {
		double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = array[i] * scaleFactor;
		return result;
	}

	private RandomAccessibleInterval<DoubleType> create(Interval target) {
		return Views.translate(ArrayImgs.doubles(Intervals.dimensionsAsLongArray(target)), Intervals
			.minAsLongArray(target));
	}
}
