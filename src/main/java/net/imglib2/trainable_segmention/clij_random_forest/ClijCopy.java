
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.HashMap;

public class ClijCopy {

	public static void copy3dStack(CLIJ2 clij, ClearCLBuffer input, ClearCLBuffer output, int offset,
		int step)
	{
		long[] globalSizes = input.getDimensions();
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src", input);
		parameters.put("dst", output);
		parameters.put("offset", offset);
		parameters.put("step", step);
		clij.execute(ClijCopy.class, "copy_3d_stack.cl", "copy_3d_stack", globalSizes, globalSizes,
			parameters);
	}
}
