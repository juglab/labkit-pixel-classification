package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.IdendityFeature;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import weka.classifiers.meta.RandomCommittee;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link Classifier}
 *
 * @author Matthias Arzt
 */
public class ClassifierTest {

	private Img<FloatType> img = ImageJFunctions.convertFloat(Utils.loadImage("nuclei.tif"));

	private ImgLabeling<String, IntType> labeling = loadLabeling("nucleiLabeling.tif");

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
		Classifier classifier = trainClassifier();
		RandomAccessibleInterval<IntType> result = classifier.apply(img);
		checkExpected(result, classifier.classNames());
	}

	private void checkExpected(RandomAccessibleInterval<IntType> result, List<String> classNames) {
		Img<UnsignedByteType> expected = ImageJFunctions.wrapByte(Utils.loadImage("nucleiExpected.tif"));
		Views.interval(Views.pair(result, expected), expected).forEach(p -> {
			String r = classNames.get(p.getA().get());
			int e = p.getB().get();
			if(e != 0) assertEquals(Integer.toString(e), r);
		});
	}

	private Classifier trainClassifier() {
		Feature feature = new FeatureGroup(new IdendityFeature(), GaussFeature.group());
		return Classifier.train(img, labeling, feature);
	}

	@Test
	public void testStoreLoad() throws IOException {
		Classifier classifier = trainClassifier();
		RandomAccessibleInterval<IntType> result = classifier.apply(img);
		classifier.store("filename");

		Classifier classifier2 = Classifier.load("filename");
		RandomAccessibleInterval<IntType> result2 = classifier2.apply(img);

		Utils.assertImagesEqual(result, result2);
	}

	@Test
	public void testGson() {
		Feature feature = new FeatureGroup(GaussFeature.single(1.0), GaussFeature.single(1.0));
		String serialized = Features.toJson(feature);
		System.out.println(serialized);
		FeatureGroup fg = Features.fromJson(serialized);
		System.out.println(fg);
	}

	@Test
	public void testDifferentWekaClassifiers() {
		Feature feature = new FeatureGroup(new IdendityFeature(), GaussFeature.group());
		Classifier classifier = Classifier.train(img, labeling, feature, new RandomCommittee());
		RandomAccessibleInterval<IntType> result = classifier.apply(img);
		checkExpected(result, classifier.classNames());
	}
}
