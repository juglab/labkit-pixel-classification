
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.util.Intervals;

import java.util.HashMap;

public class CLIJRandomForestKernel {

	public static void randomForest(CLIJ2 clij,
			ClearCLBuffer distributions,
			ClearCLBuffer src,
			ClearCLBuffer thresholds,
			ClearCLBuffer probabilities,
			ClearCLBuffer indices,
			int numberOfFeatures)
	{
		long[] globalSizes = { src.getWidth(), src.getHeight(), src.getDepth() / numberOfFeatures };
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src", src);
		parameters.put("dst", distributions);
		parameters.put("thresholds", thresholds);
		parameters.put("probabilities", probabilities);
		parameters.put("indices", indices);
		parameters.put("num_features", numberOfFeatures);
		HashMap<String, Object> constants = new HashMap<>();
		constants.put("NUMBER_OF_CLASSES", probabilities.getWidth());
		constants.put("NUMBER_OF_FEATURES", numberOfFeatures);
		constants.put("INDICES_SIZE", Intervals.numElements(indices.getDimensions()));
		clij.execute(CLIJRandomForestKernel.class, "random_forest.cl", "random_forest", globalSizes, globalSizes,
			parameters, constants);
	}

	public static void findMax(CLIJ2 clij,
		ClearCLBuffer distributions,
		ClearCLBuffer dst,
		int numberOfClasses)
	{
		long[] globalSizes = { dst.getWidth(), dst.getHeight(), dst.getDepth() };
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("dst", dst);
		parameters.put("src", distributions);
		parameters.put("num_classes", numberOfClasses);
		clij.execute(CLIJRandomForestKernel.class, "find_max.cl", "find_max", globalSizes, globalSizes,
			parameters);
	}
}
