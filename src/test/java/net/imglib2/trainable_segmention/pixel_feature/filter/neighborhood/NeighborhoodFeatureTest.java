package net.imglib2.trainable_segmention.pixel_feature.filter.neighborhood;

import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import org.junit.Test;
import org.scijava.Context;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NeighborhoodFeatureTest {

	private final OpEnvironment ops = new Context(OpService.class).service(OpService.class);

	private final NeighborhoodFeature feature = (NeighborhoodFeature)
			new FeatureSetting(NeighborhoodFeature.class, "radius", 1)
					.newInstance(ops, GlobalSettings.default2d().build());

	@Test
	public void testAttributeLabels() {
		List<String> result = feature.attributeLabels();
		List<String> expected = Arrays.asList("Neighbor (-1,-1)", "Neighbor (0,-1)", "Neighbor (1,-1)", "Neighbor (-1,0)", "Neighbor (1,0)", "Neighbor (-1,1)", "Neighbor (0,1)", "Neighbor (1,1)");
		assertEquals(expected, result);
	}

	@Test
	public void testApply() {
		final Img<FloatType> image = ArrayImgs.floats(new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, 3, 3);
		FeatureInput featureInput = new FeatureInput(image, Intervals.createMinMax(1,1,1,1));
		List<RandomAccessibleInterval<FloatType>> result = feature.apply(featureInput);
		assertEquals(1.0, Util.getTypeFromInterval(result.get(0)).get(), 0.0);
		assertEquals(6.0, Util.getTypeFromInterval(result.get(4)).get(), 0.0);
	}
}
