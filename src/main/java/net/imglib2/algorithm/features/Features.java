package net.imglib2.algorithm.features;

import net.imagej.ops.OpEnvironment;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
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

	public static FeatureGroup group(OpEnvironment ops, GlobalSettings globals, FeatureSetting... featuresSettings) {
		List<FeatureSetting> features = Arrays.asList(featuresSettings);
		return group(ops, globals, features);
	}

	public static FeatureGroup group(OpEnvironment ops, GlobalSettings globals, List<FeatureSetting> features) {
		FeatureSettings featureSettings = new FeatureSettings(globals, features);
		return group(ops, featureSettings);
	}

	private static FeatureGroup group(OpEnvironment ops, FeatureSettings featureSettings) {
		switch (featureSettings.globals().imageType()) {
			case COLOR:
				return new ColorFeatureGroup(ops, featureSettings);
			case GRAY_SCALE:
				return new GrayFeatureGroup(ops, featureSettings);
		}
		throw new AssertionError();
	}
}
