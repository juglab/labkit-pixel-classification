package net.imglib2.algorithm.features;

import net.imagej.ops.OpEnvironment;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
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

	List<FeatureOp> features();

	Class<?> getType();

	default boolean matches(RandomAccessible<?> in) {
		return getType().isInstance(in.randomAccess().get());
	}

	GlobalSettings globalSettings();
}
