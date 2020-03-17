package net.imglib2.trainable_segmention.utils;

public class FastSqrt {

	private FastSqrt() {
		// prevent from instantiation
	}

	public static float sqrt(float number) {
		return number * inverseSqrt(number);
	}

	private static float inverseSqrt(float number) {
		float x2 = number * 0.5F;
		float y = fastInverseSqrt(number);
		y = y * ( 1.5f - ( x2 * y * y ) );   // Newton
		y = y * ( 1.5f - ( x2 * y * y ) );   // Newton
		y = y * ( 1.5f - ( x2 * y * y ) );   // Newton
		y = y * ( 1.5f - ( x2 * y * y ) );   // Newton
		return y;
	}

	private static float fastInverseSqrt(float number) {
		return Float.intBitsToFloat(0x5f3759df - ( Float.floatToRawIntBits(number) >> 1 ));
	}

	public static double sqrt(double number) {
		return number * inverseSqrt(number);
	}

	private static double inverseSqrt(double number) {
		double x2 = number * 0.5;
		double y = fastInverseSqrt(number);
		y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
		y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
		y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
		y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
		y = y * ( 1.5 - ( x2 * y * y ) );   // Newton
		return y;
	}

	private static double fastInverseSqrt(double number) {
		return Double.longBitsToDouble(0x5FE6EB50C7B537A9L - ( Double.doubleToRawLongBits(number) >> 1L ));
	}
}
