
package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiChannelInputPreprocessor implements InputPreprocessor {

	private final GlobalSettings globals;

	public MultiChannelInputPreprocessor(GlobalSettings globals) {
		this.globals = globals;
	}

	@Override
	public List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input) {
		return hyperSlice(toFloat(input));
	}

	private RandomAccessible<FloatType> toFloat(RandomAccessible<?> input) {
		Object type = input.randomAccess().get();
		if (type instanceof FloatType)
			return (RandomAccessible<FloatType>) input;
		if (type instanceof RealType)
			return Converters.convert((RandomAccessible<RealType>) input,
				(in, out) -> out.setReal(in.getRealFloat()), new FloatType());
		throw new UnsupportedOperationException("Input image must be of RealType.");
	}

	private List<RandomAccessible<FloatType>> hyperSlice(RandomAccessible<FloatType> input) {
		if (input.numDimensions() != globals.numDimensions() + 1)
			throw new UnsupportedOperationException("Input image must have " + globals.numDimensions() +
				" plus one color channel.");
		int numChannels = globals.channelSetting().channels().size();
		int channelAxis = input.numDimensions() - 1;
		return IntStream.range(0, numChannels).mapToObj(channel -> Views.hyperSlice(input, channelAxis,
			channel)).collect(
				Collectors.toList());
	}

	@Override
	public Class<?> getType() {
		return RealType.class;
	}

	@Override
	public Interval outputIntervalFromInput(RandomAccessibleInterval<?> image) {
		int n = globals.numDimensions();
		if (image.numDimensions() != n + 1)
			throw new UnsupportedOperationException("Input image must have " +
				n + " plus one color channel.");
		return RevampUtils.intervalRemoveDimension(image);
	}
}
