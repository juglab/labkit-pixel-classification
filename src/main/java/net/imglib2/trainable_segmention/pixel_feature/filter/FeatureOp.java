package net.imglib2.trainable_segmention.pixel_feature.filter;

import net.imagej.ops.Op;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.plugin.SciJavaPlugin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public interface FeatureOp extends SciJavaPlugin, Op, UnaryFunctionOp<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> {

	int count();

	List<String> attributeLabels();

	default List<RandomAccessibleInterval<FloatType>> apply(FeatureInput in) {
		final List<RandomAccessibleInterval<FloatType>> outputs = createOutputs(in.targetInterval(), count());
		apply(in, outputs);
		return outputs;
	}

	void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output);

	GlobalSettings globalSettings();

	default boolean checkGlobalSettings(GlobalSettings globals) {
		return true;
	}

	static List<RandomAccessibleInterval<FloatType>> createOutputs(Interval interval, int count) {
		return IntStream.range(0, count).mapToObj( ignore -> createOutput(interval) ).collect(Collectors.toList());
	}

	static RandomAccessibleInterval<FloatType> createOutput(Interval interval) {
		return Views.translate(ArrayImgs.floats(Intervals.dimensionsAsLongArray(interval)), Intervals.minAsLongArray(interval));
	}
}
