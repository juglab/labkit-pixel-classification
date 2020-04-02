
package clij;

import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.util.Util;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gauss {

	private static Kernel1D gaussKernel(double sigma) {
		int halfsize = Util.roundToInt(sigma * 4);
		double[] values = new double[halfsize + 1];
		Arrays.setAll(values, x -> Utils.gauss(sigma, x));
		return Kernel1D.symmetric(values);
	}

	public static NeighborhoodOperation gauss(CLIJ2 clij, double... sigmas) {
		final List<CLIJKernelConvolution> convolutions = new ArrayList<>();
		for (int d = 0; d < sigmas.length; d++)
			convolutions.add(new CLIJKernelConvolution(clij, gaussKernel(sigmas[d]), d));
		return new ConcatenatedNeighborhoodOperation(clij, convolutions);
	}
}
