package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class GrayInputPreprocessor implements InputPreprocessor {

	@Override
	public List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input) {
		if(!(input.randomAccess().get() instanceof RealType))
			throw new IllegalArgumentException();
		return Collections.singletonList(RevampUtils.randomAccessibleToFloat(RevampUtils.uncheckedCast(input)));
	}

	@Override
	public Class<?> getType() {
		return RealType.class;
	}
}
