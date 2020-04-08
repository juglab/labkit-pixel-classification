
package net.imglib2.trainable_segmention.classification;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.classification.Trainer;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests {@link Segmenter}
 *
 * @author Matthias Arzt
 */
public class CLIJSegmenterTest {

	private ImgPlus<FloatType> img = VirtualStackAdapter.wrapFloat(
		new ImagePlus("/home/arzt/Documents/Datasets/Example/small-3d-stack.tif"));

	private LabelRegions<String> labeling = loadLabeling(
		"/home/arzt/Documents/Datasets/Example/small-3d-stack-labeling.tif");

	private final Context context = new Context();

	@Test
	public void testClassification() {
		Segmenter segmenter = trainClassifier();
		RandomAccessibleInterval<? extends IntegerType<?>> result = segmenter.segment(img);
		ImagePlus display = ImageJFunctions.show((RandomAccessibleInterval) result);
		display.setDisplayRange(0, 2);
	}

	@Test
	public void testSliceClassification() {
		Segmenter segmenter = trainClassifier();
		segmenter.segment(ArrayImgs.unsignedBytes(img.dimension(0), img.dimension(1), 1), Views
			.extendBorder(img));
	}

	private Segmenter trainClassifier() {
		GlobalSettings globals = GlobalSettings.default2d()
			.channels(ChannelSetting.SINGLE)
			.dimensions(img.numDimensions()).sigmas(Arrays.asList(1.0, 4.0, 8.0))
			.build();
		FeatureSettings featureSettings = new FeatureSettings(globals, // SingleFeatures.identity(),
			GroupedFeatures.gauss());
		List<String> classNames = ((LabelRegions<?>) labeling).getExistingLabels().stream().map(
			Object::toString).collect(
				Collectors.toList());
		Segmenter segmenter = new Segmenter(context, classNames, featureSettings, Trainer
			.initRandomForest());
		Trainer.of(segmenter).trainLabeledImage(img, labeling);
		return segmenter;
	}

	private static LabelRegions<String> loadLabeling(String file) {
		Img<? extends IntegerType<?>> img = ImageJFunctions.wrapByte(new ImagePlus(file));
		final ImgLabeling<String, IntType> labeling = new ImgLabeling<>(RevampUtils.createImage(img,
			new IntType()));
		Views.interval(Views.pair(img, labeling), labeling).forEach(p -> {
			int value = p.getA().getInteger();
			if (value != 0) p.getB().add(Integer.toString(value));
		});
		return new LabelRegions<>(labeling);
	}

}
