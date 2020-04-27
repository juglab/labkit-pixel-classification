
package clij;

public class ArrayUtils {

	public static int findMax(double[] doubles) {
		int maxIndex = 0;
		double max = doubles[maxIndex];
		for (int i = 1; i < doubles.length; i++) {
			if (max < doubles[i]) {
				maxIndex = i;
				max = doubles[maxIndex];
			}
		}
		return maxIndex;
	}

	static double[] add(double[] a, double[] b) {
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

	private static double sum(double[] values) {
		double sum = 0;
		for (double value : values)
			sum += value;
		return sum;
	}
}
