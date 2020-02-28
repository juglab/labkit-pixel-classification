
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

/**
 * {@link ClijLoopBuilder} provides a simple way to execute pixel wise
 * operations on images using CLIJ.
 */
public class ClijLoopBuilder {

	private static final List<String> KERNEL_PARAMETER_NAMES = Arrays.asList("a", "b", "c");

	private final CLIJ2 clij;

	private final List<String> keys = new ArrayList<>();

	private final List<ClearCLBuffer> images = new ArrayList<>();

	private ClijLoopBuilder(CLIJ2 clij) {
		this.clij = clij;
	}

	public static ClijLoopBuilder clij(CLIJ2 clij) {
		return new ClijLoopBuilder(clij);
	}

	public ClijLoopBuilder setImage(String key, ClearCLBuffer image) {
		keys.add(key);
		images.add(image);
		return this;
	}

	public void forEachPixel(String operation) {
		int n = images.size();
		if (n > KERNEL_PARAMETER_NAMES.size())
			throw new IllegalArgumentException("Two many images.");
		long[] dims = checkDimensions(keys, images);
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> defines = new HashMap<>();
		for (int i = 0; i < n; i++)
			parameters.put(KERNEL_PARAMETER_NAMES.get(i), images.get(i));
		defines.put("OPERATION_" + n + "(" + commaSeparated(keys) + ")", operation);
		clij.execute(ClijLoopBuilderTest.class, "binary_operation.cl", "operation_" + n,
			dims, dims, parameters, defines);
	}

	private static StringJoiner commaSeparated(List<String> keys) {
		StringJoiner p = new StringJoiner(",");
		for (String key : keys)
			p.add(key);
		return p;
	}

	private long[] checkDimensions(List<String> keys, List<ClearCLBuffer> images) {
		long[] dims = images.get(0).getDimensions();
		for (ClearCLBuffer image : images) {
			if (!Arrays.equals(dims, image.getDimensions()))
				wrongDimensionsError(keys, images);
		}
		return dims;
	}

	private void wrongDimensionsError(List<String> keys, List<ClearCLBuffer> images) {
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = 0; i < keys.size(); i++) {
			joiner.add("size(" + keys.get(i) + ")=" + Arrays.toString(images.get(i).getDimensions()));
		}
		throw new IllegalArgumentException("Error the sizes of the input images don't match: " +
			joiner);
	}
}
