package net.imglib2.algorithm.features;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link Classifier}
 *
 * @author Matthias Arzt
 */
public class ClassifierTest {

	static public ImgLabeling<String, IntType> loadLabeling(String file) {
		Img<? extends IntegerType<?>> img = ImageJFunctions.wrapByte(Utils.loadImage(file));
		final ImgLabeling<String, IntType> labeling = new ImgLabeling<>(RevampUtils.ops().create().img(img, new IntType()));
		Views.interval(Views.pair(img, labeling), labeling).forEach( p -> {
			int value = p.getA().getInteger();
			if(value != 0) p.getB().add(Integer.toString(value));
		} );
		return labeling;
	}

	@Test
	public void testClassification() {
		Img<FloatType> img = ImageJFunctions.convertFloat(Utils.loadImage("nuclei.tif"));
		ImgLabeling<String, IntType> labeling = loadLabeling("nucleiLabeling.tif");
		assertTrue(Intervals.equals(img, labeling));

		Feature feature = new FeatureGroup(new IdendityFeature(), GaussFeature.group());
		Classifier classifier = Classifier.train(img, labeling, feature);
		RandomAccessibleInterval<IntType> result = classifier.apply(img);

		Img<UnsignedByteType> expected = ImageJFunctions.wrapByte(Utils.loadImage("nucleiExpected.tif"));
		Views.interval(Views.pair(result, expected), expected).forEach(p -> {
			int r = p.getA().get();
			int e = p.getB().get();
			if(e == 1) assertEquals(0, r);
			if(e == 2) assertEquals(1, r);
		});
	}
}
