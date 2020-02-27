
package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

interface InputPreprocessor {

	List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input);

	Class<?> getType();

	Interval outputIntervalFromInput(RandomAccessibleInterval<?> image);
}
