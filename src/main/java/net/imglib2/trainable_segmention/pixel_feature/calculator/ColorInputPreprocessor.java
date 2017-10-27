package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public class ColorInputPreprocessor implements InputPreprocessor {

	@Override
	public List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input) {
		return RevampUtils.splitChannels(RevampUtils.castRandomAccessible(input, ARGBType.class));
	}

	@Override
	public Class<?> getType() {
		return ARGBType.class;
	}
}
