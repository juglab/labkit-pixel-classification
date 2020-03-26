
package net.imglib2.trainable_segmention.classification;

import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SegmenterTestMultiChannel {

	private OpEnvironment ops = new Context().service(OpService.class);

	private Img<UnsignedByteType> trainingImage = ArrayImgs.unsignedBytes(new byte[] {
		1, 0,
		1, 0,

		1, 1,
		0, 0,
	}, 2, 2, 2);

	private LabelRegions labeling = initLabeling();

	private LabelRegions<String> initLabeling() {
		ImgLabeling<String, ?> labeling = new ImgLabeling<>(ArrayImgs.unsignedBytes(4, 2));
		RandomAccess<LabelingType<String>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 0, 1 });
		ra.get().add("a");
		ra.setPosition(new long[] { 1, 0 });
		ra.get().add("b");
		ra.setPosition(new long[] { 0, 0 });
		ra.get().add("b");
		ra.setPosition(new long[] { 1, 1 });
		ra.get().add("b");
		return new LabelRegions<>(labeling);
	}

	@Test
	public void testSegment() {
		Segmenter segmenter = trainSegmenter();
		RandomAccessibleInterval<UnsignedByteType> result = segmenter.segment(trainingImage);
		Utils.assertImagesEqual(ArrayImgs.unsignedBytes(new byte[] { 1, 1, 0, 1 }, 2, 2), result);
	}

	private Segmenter trainSegmenter() {
		GlobalSettings globalSetting = GlobalSettings.default2d()
			.channels(ChannelSetting.multiple(2))
			.dimensions(2).sigmas(Collections.singletonList(1.0))
			.build();
		FeatureSettings features = new FeatureSettings(globalSetting,
			SingleFeatures.identity());
		return Trainer.train(ops, trainingImage, labeling, features);
	}

	@Test
	public void testPredict() {
		Segmenter segmenter = trainSegmenter();
		RandomAccessibleInterval<? extends RealType<?>> result = segmenter.predict(
			trainingImage);
		ImgLib2Assert.assertIntervalEquals(new FinalInterval(2, 2, 2), result);
		RandomAccessibleInterval<ByteType> segmentation = Converters.convert(
			Views.collapse(result),
			(in, out) -> out.setInteger(in.get(0).getRealFloat() > in.get(1).getRealFloat() ? 0 : 1),
			new ByteType());
		Utils.assertImagesEqual(ArrayImgs.bytes(new byte[] { 1, 1, 0, 1 }, 2, 2), segmentation);
	}
}
