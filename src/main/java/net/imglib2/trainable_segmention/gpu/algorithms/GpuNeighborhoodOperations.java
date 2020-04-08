
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.imglib2.trainable_segmention.gpu.api.GpuApi;

import java.util.ArrayList;
import java.util.List;

public class GpuNeighborhoodOperations {

	private GpuNeighborhoodOperations() {
		// prevent from instantiation
	}

	public static GpuNeighborhoodOperation concat(GpuApi gpu,
		List<? extends GpuNeighborhoodOperation> convolutions)
	{
		return new GpuConcatenatedNeighborhoodOperation(gpu, convolutions);
	}

	public static GpuNeighborhoodOperation min(GpuApi gpu, int[] windowSize) {
		return separableOperation(gpu, GpuSeparableOperation.Operation.MIN, windowSize);
	}

	public static GpuNeighborhoodOperation max(GpuApi gpu, int[] windowSize) {
		return separableOperation(gpu, GpuSeparableOperation.Operation.MAX, windowSize);
	}

	public static GpuNeighborhoodOperation mean(GpuApi gpu, int[] windowSize) {
		return separableOperation(gpu, GpuSeparableOperation.Operation.MEAN, windowSize);
	}

	public static GpuNeighborhoodOperation min1d(GpuApi gpu, int windowSize, int d) {
		return new GpuSeparableOperation(gpu, GpuSeparableOperation.Operation.MIN, windowSize, d);
	}

	public static GpuNeighborhoodOperation max1d(GpuApi gpu, int windowSize, int d) {
		return new GpuSeparableOperation(gpu, GpuSeparableOperation.Operation.MAX, windowSize, d);
	}

	public static GpuNeighborhoodOperation mean1d(GpuApi gpu, int windowSize, int d) {
		return new GpuSeparableOperation(gpu, GpuSeparableOperation.Operation.MEAN, windowSize, d);
	}

	// -- Helper methods --

	private static GpuNeighborhoodOperation separableOperation(GpuApi gpu,
		GpuSeparableOperation.Operation operation, int[] windowSize)
	{
		List<GpuNeighborhoodOperation> steps = new ArrayList<>();
		for (int d = 0; d < windowSize.length; d++)
			steps.add(new GpuSeparableOperation(gpu, operation, windowSize[d], d));
		return concat(gpu, steps);
	}

}
