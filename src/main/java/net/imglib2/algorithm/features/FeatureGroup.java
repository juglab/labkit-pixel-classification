package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * Created by arzt on 23.08.17.
 */
public interface FeatureGroup<T> {
	int count();

	void apply(RandomAccessible<T> in, List<RandomAccessibleInterval<FloatType>> out);

	List<String> attributeLabels();

	List<FeatureOp> features();
}
