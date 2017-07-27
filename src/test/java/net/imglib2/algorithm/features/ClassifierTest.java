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
import org.junit.Ignore;
import org.junit.Test;
import weka.classifiers.meta.RandomCommittee;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static net.imglib2.algorithm.features.GroupedFeatures.*;
import static net.imglib2.algorithm.features.SingleFeatures.*;
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
		GlobalSettings settings = new GlobalSettings(Arrays.asList(1.0, 8.0, 16.0), 3.0);
		SingleFeatures sf = new SingleFeatures(settings);
		GroupedFeatures gf = new GroupedFeatures(settings);
		FeatureGroup features = Features.group(sf.identity(), gf.gauss());
		return Classifier.train(img, labeling, features);
	}

	@Test
	public void testStoreLoad() throws IOException {
		// setup
		Classifier classifier = trainClassifier();
		// store
		File temporaryFile = File.createTempFile("classifier", ".tmp");
		classifier.store(temporaryFile.getPath());
		// load
		Classifier classifier2 = Classifier.load(temporaryFile.getPath());
		temporaryFile.delete();
		// test
		RandomAccessibleInterval<IntType> result = classifier.apply(img);
		RandomAccessibleInterval<IntType> result2 = classifier2.apply(img);
		Utils.assertImagesEqual(result, result2);
	}

	@Test
	public void testGson() {
		FeatureGroup feature = Features.group(SingleFeatures.gauss(1.0), SingleFeatures.gauss(1.0));
		String serialized = Features.toJson(feature);
		System.out.println(serialized);
		FeatureGroup fg = Features.fromJson(serialized);
		System.out.println(fg);
	}

	@Test
	public void testDifferentWekaClassifiers() {
		FeatureGroup features = Features.group(SingleFeatures.identity(), GroupedFeatures.gauss());
		Classifier classifier = Classifier.train(img, labeling, features, new RandomCommittee());
		RandomAccessibleInterval<IntType> result = classifier.apply(img);
		checkExpected(result, classifier.classNames());
	}
}
