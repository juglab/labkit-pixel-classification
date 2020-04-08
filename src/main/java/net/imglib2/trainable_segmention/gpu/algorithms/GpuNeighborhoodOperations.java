
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.imglib2.trainable_segmention.gpu.api.GpuApi;

import java.util.List;

public class GpuNeighborhoodOperations {

	private GpuNeighborhoodOperations() {
		// prevent from instantiation
	}

	public static GpuNeighborhoodOperation concat(GpuApi gpu,
		List<GpuKernelConvolution> convolutions)
	{
		return new GpuConcatenatedNeighborhoodOperation(gpu, convolutions);
	}
}
