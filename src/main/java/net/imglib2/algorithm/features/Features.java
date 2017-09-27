package net.imglib2.algorithm.features;

import net.imagej.ops.OpEnvironment;
import net.imagej.ops.special.function.Functions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class Features {

	public static <T> RandomAccessibleInterval<FloatType> applyOnImg(FeatureGroup feature, RandomAccessibleInterval<T> image) {
		return applyOnImg(feature, Views.extendBorder(image), image);
	}

	public static <T> RandomAccessibleInterval<FloatType> applyOnImg(FeatureGroup feature, RandomAccessible<T> extendedImage, Interval interval) {
		Img<FloatType> result = feature.ops().create().img(RevampUtils.extend(interval, 0, feature.count() - 1), new FloatType());
		feature.apply(extendedImage, RevampUtils.slices(result));
		return result;
	}

	public static List<Attribute> attributes(FeatureGroup feature, List<String> classes) {
		List<String> labels = feature.attributeLabels();
		List<Attribute> attributes = new ArrayList<>();
		labels.forEach(label -> attributes.add(new Attribute(label)));
		attributes.add(new Attribute("class", classes));
		return attributes;
	}

	public static <T extends FeatureOp> T create(OpEnvironment ops, Class<T> aClass, GlobalSettings globalSettings, Object... args) {
		Object[] allArgs = RevampUtils.prepend(globalSettings, args);
		return (T) (Object) Functions.unary(ops, aClass, RandomAccessibleInterval.class, RandomAccessibleInterval.class, allArgs);
	}

	public static FeatureGroup group(FeatureOp... features) {
		List<FeatureOp> t = Arrays.asList(features);
		return features[0].globalSettings().imageType().groupFactory().apply(t);
	}

	public static GrayFeatureGroup grayGroup(FeatureOp... features) {
		return new GrayFeatureGroup(Arrays.asList(features));
	}

	public static GrayFeatureGroup grayGroup(List<FeatureOp> features) {
		return new GrayFeatureGroup(features);
	}
}
