
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import java.util.ArrayList;
import java.util.List;

public class Gauss {

	public static NeighborhoodOperation gauss(GpuApi gpu, double... sigmas) {
		final List<CLIJKernelConvolution> convolutions = new ArrayList<>();
		for (int d = 0; d < sigmas.length; d++)
			convolutions.add(new CLIJKernelConvolution(gpu, gaussKernel(sigmas[d]), d));
		return new ConcatenatedNeighborhoodOperation(gpu, convolutions);
	}

	private static Kernel1D gaussKernel(double sigma) {
		return Kernel1D.symmetric(Gauss3.halfkernels(new double[] { sigma })[0]);
	}
}
