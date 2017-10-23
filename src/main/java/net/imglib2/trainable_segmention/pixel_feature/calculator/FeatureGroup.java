package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.ops.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public interface FeatureGroup {

	OpEnvironment ops();

	int count();

	void apply(RandomAccessible<?> in, List<RandomAccessibleInterval<FloatType>> out);

	List<String> attributeLabels();

	FeatureSettings settings();

	List<FeatureOp> features();

	Class<?> getType();

	default boolean matches(RandomAccessible<?> in) {
		return getType().isInstance(in.randomAccess().get());
	}
}
