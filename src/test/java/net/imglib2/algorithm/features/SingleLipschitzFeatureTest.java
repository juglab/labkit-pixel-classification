package net.imglib2.algorithm.features;

import bdv.util.BdvFunctions;
import ij.ImagePlus;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import trainableSegmentation.filters.Lipschitz_;

import java.util.Arrays;

/**
 * Tests {@link SingleLipschitzFeature}
 *
 * @author Matthias Arzt
 */
public class SingleLipschitzFeatureTest {

	public static void main(String... args) {
		performaceTest();
	}

	private static void performaceTest() {
		for (int i = 0; i < 100; i++) {
			distanceTransformOnExample(new FloatType(0), new FloatType(100));
			distanceTransformOnExample2();
		}
		Utils.TimeMeasurement.print();
	}

	private static void distanceTransformOnExample(FloatType foreground, FloatType background) {
		Img<FloatType> img = centeredDotImage(foreground, background, 2);
		Lipschitz_ lipschitz = new Lipschitz_();
		lipschitz.setTopHat(true);
		lipschitz.setDownHat(true);
		Converter<FloatType, UnsignedByteType> toByte = (in, out) -> out.set((int) in.get());
		RandomAccessibleInterval<UnsignedByteType> converted = Converters.convert((RandomAccessibleInterval<FloatType>) img, toByte, new UnsignedByteType());
		ImagePlus dot = ImageJFunctions.wrap(converted, "dot");
		Utils.TimeMeasurement.measure("legacy", () ->
		lipschitz.Lipschitz2D(dot.getProcessor()));
	}

	private static void distanceTransformOnExample2() {
		Img<FloatType> img = centeredDotImage(new FloatType(100), new FloatType(0), 2);
		Utils.TimeMeasurement.measure("forward", () ->
				new SingleLipschitzFeature(2.0).forward(Views.extendBorder(img), img));
		Utils.TimeMeasurement.measure("backward", () ->
				new SingleLipschitzFeature(2.0).backward(Views.extendBorder(img), img));
	}

	private static void showResult() {
		Img<FloatType> img = centeredDotImage(new FloatType(100), new FloatType(0), 3);
		new SingleLipschitzFeature(2.0).forward(Views.extendBorder(img), img);
		new SingleLipschitzFeature(2.0).backward(Views.extendBorder(img), img);
		BdvFunctions.show(img, "lipschitz").setDisplayRange(0, 100);
	}

	private static Img<FloatType> centeredDotImage(FloatType foreground, FloatType background, int dimension) {
		Img<FloatType> img = ArrayImgs.floats(nCopies(dimension, 100));
		img.forEach(x -> x.set(background));
		RandomAccess<FloatType> ra = img.randomAccess();
		ra.setPosition(nCopies(dimension, 50));
		ra.get().set(foreground);
		return img;
	}

	static long[] nCopies(int count, long value) {
		long[] result = new long[count];
		Arrays.fill(result, value);
		return result;
	}
}