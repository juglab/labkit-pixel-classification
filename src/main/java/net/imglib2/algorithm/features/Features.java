package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class Features {

	public static RandomAccessibleInterval<FloatType> applyOnImg(Feature feature, RandomAccessibleInterval<FloatType> image) {
		Img<FloatType> result = RevampUtils.ops().create().img(RevampUtils.extend(image, 0, feature.count() - 1), new FloatType());
		feature.apply(Views.extendBorder(image), RevampUtils.slices(result));
		return result;
	}

	public static List<Attribute> attributes(Feature feature, List<String> classes) {
		List<String> labels = feature.attributeLabels();
		List<Attribute> attributes = new ArrayList<>();
		labels.forEach(label -> attributes.add(new Attribute(label)));
		attributes.add(new Attribute("class", classes));
		return attributes;
	}
}
