
package clij;

import net.haesleinhuepf.clij2.CLIJ2;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import java.util.ArrayList;
import java.util.List;

public class Gauss {

	public static NeighborhoodOperation gauss(CLIJ2 clij, double... sigmas) {
		final List<CLIJKernelConvolution> convolutions = new ArrayList<>();
		for (int d = 0; d < sigmas.length; d++)
			convolutions.add(new CLIJKernelConvolution(clij, gaussKernel(sigmas[d]), d));
		return new ConcatenatedNeighborhoodOperation(clij, convolutions);
	}

	private static Kernel1D gaussKernel(double sigma) {
		return Kernel1D.symmetric(Gauss3.halfkernels(new double[] { sigma })[0]);
	}
}
