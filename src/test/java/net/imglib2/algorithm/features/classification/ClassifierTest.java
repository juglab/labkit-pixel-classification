package net.imglib2.algorithm.features.classification;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.*;
import net.imglib2.algorithm.features.gson.FeaturesGson;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import weka.classifiers.meta.RandomCommittee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link Classifier}
 *
 * @author Matthias Arzt
 */
public class ClassifierTest {

	private Img<FloatType> img = ImageJFunctions.convertFloat(Utils.loadImage("nuclei.tif"));

	private LabelRegions<String> labeling = loadLabeling("nucleiLabeling.tif");

	private final OpEnvironment ops = Utils.ops();

	private final SingleFeatures singleFeatures = new SingleFeatures(ops);

	private final GroupedFeatures groupedFeatures = new GroupedFeatures(ops);

	static public LabelRegions<String> loadLabeling(String file) {
		Img<? extends IntegerType<?>> img = ImageJFunctions.wrapByte(Utils.loadImage(file));
		final ImgLabeling<String, IntType> labeling = new ImgLabeling<>(Utils.ops().create().img(img, new IntType()));
		Views.interval(Views.pair(img, labeling), labeling).forEach( p -> {
			int value = p.getA().getInteger();
			if(value != 0) p.getB().add(Integer.toString(value));
		} );
		return new LabelRegions<>(labeling);
	}

	@Test
	public void testClassification() {
		Classifier classifier = trainClassifier();
		RandomAccessibleInterval<? extends IntegerType> result = classifier.segment(img);
		checkExpected(result, classifier.classNames());
	}

	private void checkExpected(RandomAccessibleInterval<? extends IntegerType> result, List<String> classNames) {
		Img<UnsignedByteType> expected = ImageJFunctions.wrapByte(Utils.loadImage("nucleiExpected.tif"));
		Views.interval(Views.pair(result, expected), expected).forEach(p -> {
			String r = classNames.get(p.getA().getInteger());
			int e = p.getB().get();
			if(e != 0) assertEquals(Integer.toString(e), r);
		});
	}

	private Classifier trainClassifier() {
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.GRAY_SCALE, Arrays.asList(1.0, 8.0, 16.0), 3.0);
		SingleFeatures sf = new SingleFeatures(ops, settings);
		GroupedFeatures gf = new GroupedFeatures(ops, settings);
		GrayFeatureGroup features = Features.grayGroup(sf.identity(), gf.gauss());
		return Trainer.train(ops, img, labeling, features);
	}

	@Test
	public void testStoreLoad() throws IOException {
		// setup
		Classifier classifier = trainClassifier();
		// store
		File temporaryFile = File.createTempFile("classifier", ".tmp");
		try( OutputStream out = new FileOutputStream(temporaryFile.getPath()) ) {
			classifier.store(out);
		}
		// load
		Classifier result1;
		try( InputStream in = new FileInputStream(temporaryFile.getPath()) ) {
			result1 = Classifier.load(ops, in);
		}
		Classifier classifier2 = result1;
		temporaryFile.delete();
		// test
		RandomAccessibleInterval<? extends IntegerType<?>> result = classifier.segment(img);
		RandomAccessibleInterval<? extends IntegerType<?>> result2 = classifier2.segment(img);
		Utils.<IntegerType>assertImagesEqual(result, result2);
	}

	@Test
	public void testGson() {
		FeatureGroup feature = Features.group(singleFeatures.gauss(1.0), singleFeatures.gauss(1.0));
		String serialized = FeaturesGson.toJson(feature);
		FeatureGroup feature2 = FeaturesGson.fromJson(serialized, ops);
		String serialized2 = FeaturesGson.toJson(feature2);
		assertEquals(serialized, serialized2);
	}

	@Test
	public void testDifferentWekaClassifiers() {
		GrayFeatureGroup features = Features.grayGroup(singleFeatures.identity(), groupedFeatures.gauss());
		Classifier classifier = Trainer.train(ops, img, labeling, features, new RandomCommittee());
		RandomAccessibleInterval<? extends IntegerType> result = classifier.segment(img);
		checkExpected(result, classifier.classNames());
	}
}
