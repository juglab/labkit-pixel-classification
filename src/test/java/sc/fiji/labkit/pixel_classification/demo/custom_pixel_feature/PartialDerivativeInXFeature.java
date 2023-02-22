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

package sc.fiji.labkit.pixel_classification.demo.custom_pixel_feature;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Plugin;
import sc.fiji.labkit.pixel_classification.gpu.GpuFeatureInput;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPixelWiseOperation;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuView;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuViews;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.AbstractFeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureInput;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A simple example to demonstrate how a new pixel feature can be added to
 * Labkit.
 * </p>
 * <p>
 * Some advices on implementing a pixel feature for Labkit: <il>
 * <li>Implement a class that extends AbstractFeatureOp</li>
 * <li>Add a "@Plugin" annotation and give it a descriptive label. (The label
 * will be visible in the UI)</li>
 * <li>The class should contain code for calculating the feature on CPU and GPU.</li>
 * <li>ImgLib2 should be used to implement the image processing on the CPU.</li>
 * <li>The GPU image processing should be implemented by using the classes in
 * the package "sc.fiji.labkit.pixel_classification.gpu" or CLIJ2.</li>
 * <li>Please write a tests for your feature. See {@link PartialDerivativeInXFeatureTest}
 * for an example.</li> </il>
 * </p>
 */
@Plugin(type = FeatureOp.class, label = "partial derivative in x")
public class PartialDerivativeInXFeature extends AbstractFeatureOp {

	@Override
	public int count() {
		return 1;
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("partial_derivative_in_x");
	}

	// CPU implementation

	/**
	 * Calculates the feature image on CPU hardware. In this case, it will calculate
	 * the partial derivative in X. And it uses imglib2's
	 * {@link SeparableKernelConvolution} to achieve this goal.
	 *
	 * @param input This parameter contains the input image, and preprocessed
	 *          versions of the input image. {@link FeatureInput#original()
	 *          input.original()} will return the input image.
	 *          {@link FeatureInput#gauss(double) input.gauss(sigma)} will will
	 *          return a gaussian blurred version of the input image.
	 * @param output Image buffers to hold the result images. The size of this list
	 *          is equal to the value returned by
	 *          {@link PartialDerivativeInXFeature#count()}. A feature can have
	 *          multiple output images.
	 * @see FeatureInput
	 */
	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		// Interval targetInterval = (Interval) output.get(0);
		Kernel1D kernel = Kernel1D.centralAsymmetric(0.5, 0.0, -0.5);
		SeparableKernelConvolution.convolution1d(kernel, 0).process(input.original(), output.get(0));
	}

	// GPU implementation

	/**
	 * This method is called before {@link #apply(GpuFeatureInput, List)}. It needs
	 * to prefetch image data that is required in the
	 * {@link #apply(GpuFeatureInput, List)} method. It should do so by calling the
	 * respective methods {@link GpuFeatureInput#prefetchOriginal(Interval)},
	 * {@link GpuFeatureInput#prefetchGauss(double, Interval)}, etc.
	 * 
	 * @param input
	 */
	@Override
	public void prefetch(GpuFeatureInput input) {
		input.prefetchOriginal(Intervals.expand(input.targetInterval(), 1));
	}

	/**
	 * Calculates the feature image on GPU hardware. In this case, the partial
	 * derivative in X is calculated.
	 *
	 * @param input This parameter contains the input image, and preprocessed
	 *          versions of the input image. {@code input.original(interval} will
	 *          return the specified part of the image.
	 * @param output The result image / images should be written into the images
	 *          contained in this list.
	 * @see GpuFeatureInput
	 * @see GpuView
	 */
	@Override
	public void apply(GpuFeatureInput input, List<GpuView> output) {
		Interval targetInterval = input.targetInterval();
		GpuView image = input.original(Intervals.expand(targetInterval, 1, 0));
		Interval center = Intervals.expand(new FinalInterval(image.dimensions()), -1, 0);
		GpuView front = GpuViews.crop(image, Intervals.translate(center, -1, 0));
		GpuView back = GpuViews.crop(image, Intervals.translate(center, 1, 0));
		GpuPixelWiseOperation.gpu(input.gpuApi())
			.addInput("a", front)
			.addInput("b", back)
			.addOutput("x", output.get(0))
			.forEachPixel("x = - 0.5 * a + 0.5 * b");
	}
}
