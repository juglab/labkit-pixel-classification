
package sc.fiji.labkit.pixel_classification.utils;

public class ArrayUtils {

	private ArrayUtils() {
		// prevent from instantiation
	}

	public static int findMax(double[] values) {
		int maxIndex = 0;
		double max = values[maxIndex];
		for (int i = 1; i < values.length; i++) {
			if (max < values[i]) {
				maxIndex = i;
				max = values[maxIndex];
			}
		}
		return maxIndex;
	}

	public static int findMax(float[] value) {
		int maxIndex = 0;
		double max = value[maxIndex];
		for (int i = 1; i < value.length; i++) {
			if (max < value[i]) {
				maxIndex = i;
				max = value[maxIndex];
			}
		}
		return maxIndex;
	}

	public static double[] add(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++)
			b[i] += a[i];
		return b;
	}

	public static float[] add(float[] a, float[] b) {
		for (int i = 0; i < a.length; i++)
			b[i] += a[i];
		return b;
	}

	public static double[] normalize(double[] values) {
		double sum = sum(values);
		for (int i = 0; i < values.length; i++) {
			values[i] /= sum;
		}
		return values;
	}

	public static float[] normalize(float[] values) {
		float sum = sum(values);
		for (int i = 0; i < values.length; i++) {
			values[i] /= sum;
		}
		return values;
	}

	public static double sum(double[] values) {
		double sum = 0;
		for (double value : values)
			sum += value;
		return sum;
	}

	public static float sum(float[] values) {
		float sum = 0;
		for (float value : values)
			sum += value;
		return sum;
	}

	public static float[] toFloats(double[] values) {
		float[] result = new float[values.length];
		for (int i = 0; i < result.length; i++)
			result[i] = (float) values[i];
		return result;
	}
}
