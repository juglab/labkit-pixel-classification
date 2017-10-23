package net.imglib2.trainable_segmention.ops;

import ij.ImagePlus;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

/**
 * @author Matthias Arzt
 */
public class SingleGaborFeatureTest {

	@Test
	public void testNormalize() {
		ImagePlus image = Utils.loadImage("nuclei.tif");
		Img<FloatType> expected = ImageJFunctions.wrapFloat(new ImagePlus("", trainableSegmentation.utils.Utils.normalize(image.getStack())));
		Img<FloatType> result = Utils.copy(ImageJFunctions.convertFloat(image));
		SingleGaborFeature.normalize(Utils.ops(), result);
		Utils.assertImagesEqual(expected, result);
	}
}
