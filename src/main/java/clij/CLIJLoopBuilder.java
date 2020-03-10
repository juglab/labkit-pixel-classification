
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * {@link CLIJLoopBuilder} provides a simple way to execute pixel wise
 * operations on images using CLIJ.
 */
public class CLIJLoopBuilder {

	private final CLIJ2 clij;

	private final List<String> parameterDefinition = new ArrayList<>();
	private final List<String> preOperation = new ArrayList<>();
	private final List<String> postOperation = new ArrayList<>();
	private final HashMap<String, Object> parameterValues = new HashMap<>();
	private final Map<ClearCLBuffer, String> images = new HashMap<>();
	private final List<ValuePair<String, long[]>> imageSizes = new ArrayList<>();

	private CLIJLoopBuilder(CLIJ2 clij) {
		this.clij = clij;
	}

	public static CLIJLoopBuilder clij(CLIJ2 clij) {
		return new CLIJLoopBuilder(clij);
	}

	public CLIJLoopBuilder addInput(String variable, int image) {
		addParameter("int", variable, image);
		return this;
	}

	public CLIJLoopBuilder addInput(String variable, long image) {
		addParameter("long", variable, image);
		return this;
	}

	public CLIJLoopBuilder addInput(String variable, float image) {
		addParameter("float", variable, image);
		return this;
	}

	public CLIJLoopBuilder addInput(String variable, double image) {
		addParameter("double", variable, image);
		return this;
	}

	public CLIJLoopBuilder addInput(String variable, ClearCLBuffer image) {
		String parameterName = addImageParameter(image);
		registerSize(variable, image.getDimensions());
		preOperation.add("IMAGE_" + parameterName + "_PIXEL_TYPE " + variable + " = PIXEL(" +
			parameterName + ")");
		return this;
	}

	public CLIJLoopBuilder addOutput(String variable, ClearCLBuffer image) {
		String parameterName = addImageParameter(image);
		registerSize(variable, image.getDimensions());
		preOperation.add("IMAGE_" + parameterName + "_PIXEL_TYPE " + variable + " = 0");
		postOperation.add("PIXEL(" + parameterName + ") = " + variable);
		return this;
	}

	public CLIJLoopBuilder addInput(String variable, CLIJView image) {
		String parameterName = addCLIJViewParameters(variable, image);
		preOperation.add("IMAGE_" + parameterName + "_PIXEL_TYPE " + variable + " = " +
			pixelAt(parameterName, image.interval()));
		return this;
	}

	public CLIJLoopBuilder addOutput(String variable, CLIJView image) {
		String parameterName = addCLIJViewParameters(variable, image);
		preOperation.add("IMAGE_" + parameterName + "_PIXEL_TYPE " + variable + " = 0");
		postOperation.add(pixelAt(parameterName, image.interval()) + " = " + variable);
		return this;
	}

	private String pixelAt(String parameterName, Interval offset) {
		return "PIXEL_AT(" + parameterName + ", x + " + getMin(offset, 0) + ", y + " + getMin(offset,
			1) +
			", z + " + getMin(offset, 2) + ")";
	}

	private String addCLIJViewParameters(String variable, CLIJView image) {
		String parameterName = addImageParameter(image.buffer());
		registerSize(variable, Intervals.dimensionsAsLongArray(image.interval()));
		return parameterName;
	}

	private void registerSize(String variable, long[] size) {
		imageSizes.add(new ValuePair<>(variable, size));
	}

	private long getMin(Interval interval, int d) {
		return d < interval.numDimensions() ? interval.min(d) : 0;
	}

	private String addImageParameter(ClearCLBuffer image) {
		if (images.containsKey(image))
			return images.get(image);
		else {
			String parameterName = "image_" + (images.size() + 1);
			images.put(image, parameterName);
			addParameter("IMAGE_" + parameterName + "_TYPE ", parameterName, image);
			return parameterName;
		}
	}

	private void addParameter(String parameterType, String parameterName, Object value) {
		parameterDefinition.add(parameterType + " " + parameterName);
		parameterValues.put(parameterName, value);
	}

	public void forEachPixel(String operation) {
		long[] dims = checkDimensions();
		HashMap<String, Object> defines = new HashMap<>();
		defines.put("PARAMETER", concatenate(", ", parameterDefinition));
		defines.put("OPERATION", concatenate("; ", preOperation, Collections.singletonList(operation),
			postOperation));
		clij.execute(CLIJLoopBuilder.class, "binary_operation.cl", "operation",
			dims, dims, parameterValues, defines);
	}

	private static String concatenate(String delimiter, List<String>... lists) {
		StringJoiner joiner = new StringJoiner(delimiter);
		for (List<String> values : lists)
			values.forEach(joiner::add);
		return joiner.toString();
	}

	private long[] checkDimensions() {
		long[] dims = imageSizes.get(0).getB();
		for (ValuePair<String, long[]> image : imageSizes) {
			if (!Arrays.equals(dims, image.getB()))
				wrongDimensionsError();
		}
		return dims;
	}

	private void wrongDimensionsError() {
		StringJoiner joiner = new StringJoiner(" ");
		for (ValuePair<String, long[]> pair : imageSizes) {
			String imageName = pair.getA();
			long[] imageSize = pair.getB();
			joiner.add("size(" + imageName + ")=" + Arrays.toString(imageSize));
		}
		throw new IllegalArgumentException("Error the sizes of the input images don't match: " +
			joiner);
	}
}