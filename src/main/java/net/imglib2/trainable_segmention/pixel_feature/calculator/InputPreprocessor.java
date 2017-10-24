package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imglib2.RandomAccessible;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

interface InputPreprocessor {

	GlobalSettings.ImageType getImageType();

	List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input);

	Class<?> getType();
}
