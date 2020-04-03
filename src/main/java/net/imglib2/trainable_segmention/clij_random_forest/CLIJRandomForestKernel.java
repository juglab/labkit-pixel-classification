
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import clij.GpuApi;
import net.imglib2.util.Intervals;

import java.util.HashMap;

public class CLIJRandomForestKernel {

	public static void randomForest(GpuApi gpu,
		CLIJMultiChannelImage distributions,
		CLIJMultiChannelImage src,
		ClearCLBuffer thresholds,
		ClearCLBuffer probabilities,
		ClearCLBuffer indices,
		int numberOfFeatures)
	{
		long[] globalSizes = src.getSpatialDimensions().clone();
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src", src.asClearCLBuffer());
		parameters.put("dst", distributions.asClearCLBuffer());
		parameters.put("thresholds", thresholds);
		parameters.put("probabilities", probabilities);
		parameters.put("indices", indices);
		HashMap<String, Object> constants = new HashMap<>();
		constants.put("NUMBER_OF_CLASSES", probabilities.getWidth());
		constants.put("NUMBER_OF_FEATURES", numberOfFeatures);
		constants.put("INDICES_SIZE", Intervals.numElements(indices.getDimensions()));
		gpu.execute(CLIJRandomForestKernel.class, "random_forest.cl", "random_forest", globalSizes,
			parameters, constants);
	}

	public static void findMax(GpuApi gpu,
		CLIJMultiChannelImage distributions,
		ClearCLBuffer dst)
	{
		long[] globalSizes = { dst.getWidth(), dst.getHeight(), dst.getDepth() };
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("dst", dst);
		parameters.put("src", distributions.asClearCLBuffer());
		parameters.put("num_classes", (int) distributions.getNChannels());
		gpu.execute(CLIJRandomForestKernel.class, "find_max.cl", "find_max", globalSizes,
			parameters, null);
	}
}
