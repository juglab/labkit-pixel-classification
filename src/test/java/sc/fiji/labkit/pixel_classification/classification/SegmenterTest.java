/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
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

import com.google.gson.JsonElement;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.deprecated.gauss.GaussFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.ChannelSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.GroupedFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.utils.CpuGpuRunner;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scijava.Context;
import weka.classifiers.meta.RandomCommittee;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

/**
 * Tests {@link Segmenter}
 *
 * @author Matthias Arzt
 */
@RunWith(CpuGpuRunner.class)
public class SegmenterTest {

	public final boolean useGpu;

	public SegmenterTest(boolean useGpu) {
		this.useGpu = useGpu;
	}

	private Img<FloatType> img = ImageJFunctions.convertFloat(Utils.loadImage("nuclei.tif"));

	private Img<FloatType> expectedProbabilityMap = ImageJFunctions.convertFloat(Utils.loadImage("nucleiProbabilityMap.tif"));

	private LabelRegions<String> labeling = loadLabeling("nucleiLabeling.tif");

	private final Context context = SingletonContext.getInstance();

	@Test
	public void testClassification() {
		Segmenter segmenter = trainClassifier();
		segmenter.setUseGpu(useGpu);
		RandomAccessibleInterval<? extends IntegerType<?>> result = segmenter.segment(img);
		checkExpected(result, segmenter.classNames());
	}

	@Test
	public void testPredict() {
		Segmenter segmenter = trainClassifier();
		segmenter.setUseGpu(useGpu);
		RandomAccessibleInterval<? extends RealType<?>> probabilityMap = segmenter.predict(img);
		ImgLib2Assert.assertImageEqualsRealType(expectedProbabilityMap, probabilityMap, 0.001);
	}

	private Segmenter trainClassifier() {
		GlobalSettings globals = GlobalSettings.default2d()
			.channels(ChannelSetting.SINGLE)
			.dimensions(img.numDimensions()).sigmas(Arrays.asList(1.0, 4.0, 8.0))
			.build();
		FeatureSettings featureSettings = new FeatureSettings(globals, SingleFeatures.identity(),
			GroupedFeatures.gauss());
		return Trainer.train(context, img, labeling, featureSettings);
	}

	private void checkExpected(RandomAccessibleInterval<? extends IntegerType<?>> result,
		List<String> classNames)
	{
		Img<UnsignedByteType> expected = ImageJFunctions.wrapByte(Utils.loadImage(
			"nucleiExpected.tif"));
		Views.interval(Views.pair(result, expected), expected).forEach(p -> {
			String r = classNames.get(p.getA().getInteger());
			int e = p.getB().get();
			if (e != 0) assertEquals(Integer.toString(e), r);
		});
	}

	@Test
	public void testStoreLoad() throws IOException {
		// setup
		Segmenter segmenter = trainClassifier();
		// store
		JsonElement json = segmenter.toJsonTree();
		// load
		Segmenter segmenter2 = Segmenter.fromJson(context, json);
		// test
		RandomAccessibleInterval<? extends IntegerType<?>> result = segmenter.segment(img);
		RandomAccessibleInterval<? extends IntegerType<?>> result2 = segmenter2.segment(img);
		Utils.<IntegerType> assertImagesEqual(result, result2);
	}

	@Ignore("Only fast random forest is supported")
	@Test
	public void testDifferentWekaClassifiers() {
		assumeFalse(useGpu);
		FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default2d().build(),
			SingleFeatures.identity(), new FeatureSetting(GaussFeature.class));
		Segmenter segmenter = Trainer.train(context, img, labeling, featureSettings,
			new RandomCommittee());
		RandomAccessibleInterval<? extends IntegerType<?>> result = segmenter.segment(img);
		checkExpected(result, segmenter.classNames());
	}

	private static LabelRegions<String> loadLabeling(String file) {
		Img<? extends IntegerType<?>> img = ImageJFunctions.wrapByte(Utils.loadImage(file));
		final ImgLabeling<String, IntType> labeling = new ImgLabeling<>(RevampUtils.createImage(img,
			new IntType()));
		Views.interval(Views.pair(img, labeling), labeling).forEach(p -> {
			int value = p.getA().getInteger();
			if (value != 0) p.getB().add(Integer.toString(value));
		});
		return new LabelRegions<>(labeling);
	}

}
