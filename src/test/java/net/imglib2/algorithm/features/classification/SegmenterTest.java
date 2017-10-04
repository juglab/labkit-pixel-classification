package net.imglib2.algorithm.features.classification;

import com.google.gson.JsonElement;
import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.*;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link Segmenter}
 *
 * @author Matthias Arzt
 */
public class SegmenterTest {

	private Img<FloatType> img = ImageJFunctions.convertFloat(Utils.loadImage("nuclei.tif"));

	private LabelRegions<String> labeling = loadLabeling("nucleiLabeling.tif");

	private final OpEnvironment ops = Utils.ops();

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
		Segmenter segmenter = trainClassifier();
		RandomAccessibleInterval<? extends IntegerType> result = segmenter.segment(img);
		checkExpected(result, segmenter.classNames());
	}

	private void checkExpected(RandomAccessibleInterval<? extends IntegerType> result, List<String> classNames) {
		Img<UnsignedByteType> expected = ImageJFunctions.wrapByte(Utils.loadImage("nucleiExpected.tif"));
		Views.interval(Views.pair(result, expected), expected).forEach(p -> {
			String r = classNames.get(p.getA().getInteger());
			int e = p.getB().get();
			if(e != 0) assertEquals(Integer.toString(e), r);
		});
	}

	private Segmenter trainClassifier() {
		GlobalSettings settings = new GlobalSettings(GlobalSettings.ImageType.GRAY_SCALE, Arrays.asList(1.0, 8.0, 16.0), 3.0);
		FeatureGroup features = Features.group(Utils.ops(), settings, SingleFeatures.identity(), GroupedFeatures.gauss());
		return Trainer.train(ops, img, labeling, features);
	}

	@Test
	public void testStoreLoad() throws IOException {
		// setup
		Segmenter segmenter = trainClassifier();
		// store
		File temporaryFile = File.createTempFile("classifier", ".tmp");
		JsonElement json = segmenter.toJsonTree();
		// load
		Segmenter segmenter2 = Segmenter.fromJson(ops, json);
		temporaryFile.delete();
		// test
		RandomAccessibleInterval<? extends IntegerType<?>> result = segmenter.segment(img);
		RandomAccessibleInterval<? extends IntegerType<?>> result2 = segmenter2.segment(img);
		Utils.<IntegerType>assertImagesEqual(result, result2);
	}

	@Test
	public void testDifferentWekaClassifiers() {
		FeatureGroup features = Features.group(Utils.ops(), GlobalSettings.defaultSettings(), SingleFeatures.identity(), GroupedFeatures.gauss());
		Segmenter segmenter = Trainer.train(ops, img, labeling, features, new RandomCommittee());
		RandomAccessibleInterval<? extends IntegerType> result = segmenter.segment(img);
		checkExpected(result, segmenter.classNames());
	}
}
