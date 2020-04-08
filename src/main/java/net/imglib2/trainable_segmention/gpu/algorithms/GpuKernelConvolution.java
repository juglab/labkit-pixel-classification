
package net.imglib2.trainable_segmention.gpu.algorithms;

import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.gpu.api.GpuApi;
import net.imglib2.trainable_segmention.gpu.api.GpuImage;
import net.imglib2.trainable_segmention.gpu.api.GpuView;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.apache.commons.lang3.ArrayUtils;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GpuKernelConvolution implements GpuNeighborhoodOperation {

	private final GpuApi gpu;

	private final Kernel1D kernel;

	private final int d;

	public GpuKernelConvolution(GpuApi gpu, Kernel1D kernel, int d) {
		this.gpu = gpu;
		this.kernel = kernel;
		this.d = d;
	}

	@Override
	public Interval getRequiredInputInterval(Interval targetInterval) {
		final long[] min = Intervals.minAsLongArray(targetInterval);
		final long[] max = Intervals.maxAsLongArray(targetInterval);
		min[d] += kernel.min();
		max[d] += kernel.max();
		return new FinalInterval(min, max);
	}

	@Override
	public void convolve(GpuView input, GpuView output) {
		final Img<DoubleType> kernelImage = ArrayImgs.doubles(kernel.fullKernel(), kernel.size());
		try (GpuImage kernelBuffer = gpu.push(RealTypeConverters.convert(kernelImage,
			new FloatType())))
		{
			convolve(gpu, input, kernelBuffer, output, d);
		}
	}

	public static void convolve(GpuApi gpu, GpuView input, GpuImage kernel, GpuView output, int d) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("input", input.source());
		parameters.put("kernelValues", kernel);
		parameters.put("output", output.source());
		HashMap<String, Object> defines = new HashMap<>();
		long[] localSizes = new long[3];
		Arrays.fill(localSizes, 1);
		localSizes[0] = output.dimensions().dimension(d);
		defines.put("KERNEL_LENGTH", kernel.getWidth());
		defines.put("BLOCK_SIZE", localSizes[0]);
		setSkips(defines, "INPUT", input, d);
		setSkips(defines, "OUTPUT", output, d);
		defines.put("OUTPUT_IMAGE_PARAMETER", "__write_only __global float* output");
		defines.put("INPUT_IMAGE_PARAMETER", "__read_only __global float* input");
		defines.put("OUTPUT_WRITE_PIXEL(x,y,z,v)",
			"output[OUTPUT_OFFSET + OUTPUT_X_SKIP * (x) + OUTPUT_Y_SKIP * (y) + OUTPUT_Z_SKIP * (z)] = v;");
		defines.put("INPUT_READ_PIXEL(x,y,z)",
			"input[INPUT_OFFSET + INPUT_X_SKIP * (x) + INPUT_Y_SKIP * (y) + INPUT_Z_SKIP * (z)]");
		long[] globalSizes = getDimensions(output.dimensions());
		ArrayUtils.swap(globalSizes, 0, d);
		gpu.execute(GpuKernelConvolution.class, "gauss.cl", "convolve1d",
			globalSizes, localSizes, parameters, defines);
	}

	public static void convolve(GpuApi gpu, GpuImage input, GpuImage kernel, int kernel_center,
		GpuImage output, int d)
	{
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("input", input);
		parameters.put("kernelValues", kernel);
		parameters.put("output", output);
		HashMap<String, Object> defines = new HashMap<>();
		long[] localSizes = new long[3];
		Arrays.fill(localSizes, 1);
		localSizes[0] = output.getDimensions()[d];
		defines.put("KERNEL_LENGTH", kernel.getWidth());
		defines.put("BLOCK_SIZE", localSizes[0]);
		defines.put("OUTPUT_IMAGE_PARAMETER", "IMAGE_output_TYPE output");
		defines.put("INPUT_IMAGE_PARAMETER", "IMAGE_input_TYPE input");
		defines.put("OUTPUT_WRITE_PIXEL(cx,cy,cz,v)",
			"WRITE_output_IMAGE(output, POS_output_INSTANCE(" + position(d, 0) + ",0), v)");
		defines.put("INPUT_READ_PIXEL(cx,cy,cz)",
			"READ_input_IMAGE(input, sampler, POS_input_INSTANCE(" + position(d, kernel_center) +
				",0)).x");
		long[] globalSizes = output.getDimensions();
		ArrayUtils.swap(globalSizes, 0, d);
		gpu.execute(GpuKernelConvolution.class, "gauss.cl", "convolve1d",
			globalSizes, localSizes, parameters, defines);
	}

	private static String position(int d, int kernel_center) {
		List<String> list = new ArrayList<>(Arrays.asList("(cx) - " + kernel_center, "(cy)", "(cz)"));
		Collections.swap(list, 0, d);
		return "(" + list.get(0) + "),(" + list.get(1) + "),(" + list.get(2) + ")";
	}

	private static void setSkips(HashMap<String, Object> defines, String prefix, GpuView view,
		int d)
	{
		GpuImage buffer = view.source();
		long[] skip = { 1, buffer.getWidth(), buffer.getWidth() * buffer.getHeight() };
		defines.put(prefix + "_OFFSET", view.offset());
		ArrayUtils.swap(skip, 0, d);
		defines.put(prefix + "_X_SKIP", skip[0]);
		defines.put(prefix + "_Y_SKIP", skip[1]);
		defines.put(prefix + "_Z_SKIP", skip[2]);
	}

	private static long getOffset(Interval interval, long[] skip) {
		long result = 0;
		for (int i = 0; i < interval.numDimensions(); i++) {
			result += skip[i] * interval.min(i);
		}
		return result;
	}

	private static long[] getDimensions(Dimensions dimensions) {
		return new long[] {
			getDimension(dimensions, 0),
			getDimension(dimensions, 1),
			getDimension(dimensions, 2),
		};
	}

	private static long getDimension(Dimensions dimensions, int d) {
		return d < dimensions.numDimensions() ? dimensions.dimension(d) : 1;
	}
}
