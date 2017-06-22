package net.imglib2.algorithm.features;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public interface Feature {

	int count();

	void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out);

	List<String> attributeLabels();
}
