/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.classification;

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
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.ChannelSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import sc.fiji.labkit.pixel_classification.utils.views.FastViews;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SegmenterTestMultiChannel {

	private Context context = SingletonContext.getInstance();

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
		return Trainer.train(context, trainingImage, labeling, features);
	}

	@Test
	public void testPredict() {
		Segmenter segmenter = trainSegmenter();
		RandomAccessibleInterval<? extends RealType<?>> result = segmenter.predict(
			trainingImage);
		ImgLib2Assert.assertIntervalEquals(new FinalInterval(2, 2, 2), result);
		RandomAccessibleInterval<ByteType> segmentation = Converters.convert(
			FastViews.collapse(result),
			(in, out) -> out.setInteger(in.get(0).getRealFloat() > in.get(1).getRealFloat() ? 0 : 1),
			new ByteType());
		Utils.assertImagesEqual(ArrayImgs.bytes(new byte[] { 1, 1, 0, 1 }, 2, 2), segmentation);
	}
}
