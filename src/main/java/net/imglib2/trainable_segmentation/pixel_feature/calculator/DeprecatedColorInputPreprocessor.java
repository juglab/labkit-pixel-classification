
package net.imglib2.trainable_segmentation.pixel_feature.calculator;

import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.List;

/**
 * Similar to {@link ColorInputPreprocessor} but scales image intensities by a
 * value of 1 / 255. Only kept to allow backward compatibility for old models.
 *
 * @author Matthias Arzt
 */
public class DeprecatedColorInputPreprocessor extends ColorInputPreprocessor {

	public DeprecatedColorInputPreprocessor(GlobalSettings globals) {
		super(globals);
	}

	protected List<RandomAccessible<FloatType>> processARGBType(
			RandomAccessible< ARGBType > image) {
		if (image.numDimensions() != globals.numDimensions())
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() +
				" dimensions.");
		return Arrays.asList(
				Converters.convert(image, (in, out) -> out.setReal(ARGBType.red(in.get()) / 255.f), new FloatType()),
				Converters.convert(image, (in, out) -> out.setReal(ARGBType.green(in.get()) / 255.f), new FloatType()),
				Converters.convert(image, (in, out) -> out.setReal(ARGBType.blue(in.get()) / 255.f), new FloatType())
		);
	}

	protected List<RandomAccessible<FloatType>> processRealType(
			RandomAccessible< FloatType > image) {
		// TODO check number of channels
		int colorAxis = globals.numDimensions();
		if (image.numDimensions() != colorAxis + 1)
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() +
				" dimensions plus one color channel.");
		return Arrays.asList(
				sliceAndScale(image, colorAxis, 0),
				sliceAndScale(image, colorAxis, 1),
				sliceAndScale(image, colorAxis, 2)
		);
	}

	private RandomAccessible< FloatType > sliceAndScale(
			RandomAccessible< FloatType > image, int colorAxis, int pos)
	{
		MixedTransformView< FloatType > slice = Views.hyperSlice(image, colorAxis, pos);
		Converter< FloatType, FloatType > scale = (in, out) -> out.setReal(in.getRealFloat() / 255.f);
		return Converters.convert(slice, scale, new FloatType());
	}
}
