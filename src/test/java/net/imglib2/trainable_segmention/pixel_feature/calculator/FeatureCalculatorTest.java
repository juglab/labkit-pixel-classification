package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FeatureCalculatorTest {

	OpService ops = new Context().service(OpService.class);

	FeatureSetting add_42 = new FeatureSetting(AddValue.class, "value", 42);
	FeatureSetting add_12 = new FeatureSetting(AddValue.class, "value", 12);

	@Test
	public void test1() {
		GlobalSettings globalSettings = GlobalSettings.default2d().build();
		FeatureSettings settings = new FeatureSettings(globalSettings, add_42, add_12);
		FeatureCalculator calculator = new FeatureCalculator(ops, settings);
		Img< FloatType > input = ArrayImgs.floats(new float[] { 2 }, 1, 1);
		RandomAccessibleInterval< FloatType > out = calculator.apply(input);
		Utils.assertImagesEqual(ArrayImgs.floats(new float[]{44, 14}, 1, 1, 2), out);
	}

	@Test
	public void test2() {
		GlobalSettings globalSettings = GlobalSettings.default2d().channels(ChannelSetting.multiple(2)).radii(1.0).build();
		FeatureSettings settings = new FeatureSettings(globalSettings, add_42, add_12);
		FeatureCalculator calculator = new FeatureCalculator(ops, settings);
		Img< FloatType > input = ArrayImgs.floats(new float[] { 2, 3 }, 1, 1, 2);
		RandomAccessibleInterval< FloatType > out = calculator.apply(input);
		assertEquals(Arrays.asList("channel1_add_value_42.0", "channel2_add_value_42.0", "channel1_add_value_12.0", "channel2_add_value_12.0"),
				calculator.attributeLabels());
		Utils.assertImagesEqual(ArrayImgs.floats(new float[]{44, 45, 14, 15}, 1, 1, 4), out);
	}

	public static class AddValue extends AbstractFeatureOp implements FeatureOp {

		@Parameter
		double value;

		@Override public int count() {
			return 1;
		}

		@Override public List< String > attributeLabels() {
			return Collections.singletonList("add_value_" + value);
		}

		@Override public void apply(FeatureInput input,
				List< RandomAccessibleInterval< FloatType > > output)
		{
			IntervalView< FloatType > inputInterval = Views.interval(input.original(), output.get(0));
			LoopBuilder.setImages(inputInterval, output.get(0) ).forEachPixel(
					(in, out) -> out.set( in.get() + (float) value ));
		}
	}
}
