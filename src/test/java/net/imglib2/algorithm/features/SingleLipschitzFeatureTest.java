package net.imglib2.algorithm.features;

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
import trainableSegmentation.filters.Lipschitz_;

/**
 * Tests {@link SingleLipschitzFeature}
 *
 * @author Matthias Arzt
 */
public class SingleLipschitzFeatureTest {

	public static void main(String... args) {
		legacyLipschitz(0, 255, true, true).show();
		legacyLipschitz(0, 255, false, true).show();
		legacyLipschitz(0, 255, true, false).show();
		legacyLipschitz(0, 255, false, false).show();
		legacyLipschitz(255, 0, true, true).show();
		legacyLipschitz(255,0 , false, true).show();
		legacyLipschitz(255,0 , true, false).show();
		legacyLipschitz(255,0 , false, false).show();
		// -> topHat means substract result from the original image
		// -> downHat means to use minimum operation instead
	}

	private static void singleForwardTest() {
		Img<FloatType> img = centeredDotImage(100, 0, 2);
		new SingleLipschitzFeature(2.0, 0).lipschitz(img);
		ImageJFunctions.show(img);
	}

	private static void performaceTest() {
		for (int i = 0; i < 100; i++) {
			legacyLipschitz(100, 0, true, true);
			distanceTransformOnExample2();
		}
		Utils.TimeMeasurement.print();
	}

	private static ImagePlus legacyLipschitz(float fg, float bg, final boolean topHat, final boolean downHat) {
		Img<FloatType> img = centeredDotImage(fg, bg, 2);
		ImagePlus dot = wrapByte(img);
		Utils.TimeMeasurement.measure("legacy", () -> {
			Lipschitz_ lipschitz = new Lipschitz_();
			lipschitz.setTopHat(topHat);
			lipschitz.setDownHat(downHat);
			lipschitz.Lipschitz2D(dot.getProcessor());
		});
		return dot;
	}

	private static ImagePlus wrapByte(RandomAccessibleInterval<FloatType> img) {
		Converter<FloatType, UnsignedByteType> toByte = (in, out) -> out.set((int) in.get());
		RandomAccessibleInterval<UnsignedByteType> converted = Converters.convert(img, toByte, new UnsignedByteType());
		return ImageJFunctions.wrap(converted, "dot");
	}

	private static void distanceTransformOnExample2() {
		Img<FloatType> img = centeredDotImage(100, 0, 2);
		Utils.TimeMeasurement.measure("new", () -> new SingleLipschitzFeature(1.0, 0).lipschitz(img));
	}

	private static Img<FloatType> centeredDotImage(float foreground, float background, int dimension) {
		Img<FloatType> img = ArrayImgs.floats(RevampUtils.nCopies(dimension, 100));
		img.forEach(x -> x.set(background));
		RandomAccess<FloatType> ra = img.randomAccess();
		ra.setPosition(RevampUtils.nCopies(dimension, 50));
		ra.get().set(foreground);
		return img;
	}
}