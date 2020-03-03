
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import java.util.HashMap;

public class CLIJCopy {

	public static void copy3dStack(CLIJ2 clij, ClearCLBuffer input, ClearCLBuffer output, int offset,
		int step)
	{
		long[] globalSizes = input.getDimensions();
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src", input);
		parameters.put("dst", output);
		parameters.put("offset", offset);
		parameters.put("step", step);
		clij.execute(CLIJCopy.class, "copy_3d_stack.cl", "copy_3d_stack", globalSizes, globalSizes,
			parameters);
	}

	public static void copy(CLIJ2 clij, CLIJView src, CLIJView dst) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src_offset_x", min(src, 0));
		parameters.put("src_offset_y", min(src, 1));
		parameters.put("src_offset_z", min(src, 2));
		parameters.put("dst_offset_x", min(dst, 0));
		parameters.put("dst_offset_y", min(dst, 1));
		parameters.put("dst_offset_z", min(dst, 2));
		parameters.put("src", src.buffer());
		parameters.put("dst", dst.buffer());
		long[] globalSizes = Intervals.dimensionsAsLongArray(src.interval());
		if (!Intervals.equalDimensions(src.interval(), dst.interval()))
			throw new IllegalArgumentException();
		clij.execute(CLIJCopy.class, "copy_with_offset.cl", "copy_with_offset", globalSizes,
			globalSizes,
			parameters);
	}

	private static int min(CLIJView view, int d) {
		Interval interval = view.interval();
		return (int) (d < interval.numDimensions() ? interval.min(d) : 0);
	}
}
