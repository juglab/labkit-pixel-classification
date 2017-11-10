package net.imglib2.trainable_segmention.pixel_feature.calculator;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class ColorInputPreprocessor implements InputPreprocessor {

	private final GlobalSettings globals;

	public ColorInputPreprocessor(GlobalSettings globals) {
		this.globals = globals;
	}

	@Override
	public List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input) {
		Object element = input.randomAccess().get();
		if(element instanceof ARGBType)
			return processARGBType(RevampUtils.uncheckedCast(input));
		else if(element instanceof RealType)
			return processRealType(RevampUtils.randomAccessibleToFloat(RevampUtils.uncheckedCast(input)));
		throw new IllegalArgumentException("Input image must be RealType or ARGBType.");
	}

	private List<RandomAccessible<FloatType>> processARGBType(RandomAccessible<ARGBType> image) {
		return RevampUtils.splitChannels(image);
	}

	private List<RandomAccessible<FloatType>> processRealType(RandomAccessible<FloatType> image) {
		// TODO check number of channels
		int colorAxis = globals.numDimensions();
		if(image.numDimensions() != colorAxis + 1)
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() + " dimensions plus one color channel.");
		MixedTransformView<FloatType> red = Views.hyperSlice(image, colorAxis, 0);
		MixedTransformView<FloatType> green = Views.hyperSlice(image, colorAxis, 1);
		MixedTransformView<FloatType> blue = Views.hyperSlice(image, colorAxis, 2);
		return Arrays.asList(red, green, blue);
	}


	@Override
	public Class<?> getType() {
		return ARGBType.class;
	}

	@Override
	public Interval outputIntervalFromInput(RandomAccessibleInterval<?> image) {
		Object element = image.randomAccess().get();
		if(element instanceof ARGBType)
			return image;
		else if(element instanceof RealType)
			return outputIntervalFromInputRealType(image);
		return null;
	}

	private Interval outputIntervalFromInputRealType(RandomAccessibleInterval<?> image) {
		int colorAxis = globals.numDimensions();
		if(image.numDimensions() != colorAxis + 1)
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() + " dimensions plus one color channel.");
		if(image.dimension(colorAxis) != 3)
			throw new IllegalArgumentException("Input image must contain three color channels.");
		return new FinalInterval(Arrays.copyOf(Intervals.minAsLongArray(image), colorAxis),
				Arrays.copyOf(Intervals.maxAsLongArray(image), colorAxis));
	}
}
