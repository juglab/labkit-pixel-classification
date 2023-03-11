/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.pixel_feature.calculator;

import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
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
		RandomAccessible<ARGBType> image)
	{
		if (image.numDimensions() != globals.numDimensions())
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() +
				" dimensions.");
		Converter<ARGBType, FloatType> r = (in, out) -> out.setReal(ARGBType.red(in.get()) / 255.f);
		Converter<ARGBType, FloatType> g = (in, out) -> out.setReal(ARGBType.green(in.get()) / 255.f);
		Converter<ARGBType, FloatType> b = (in, out) -> out.setReal(ARGBType.blue(in.get()) / 255.f);
		return Arrays.asList(
			Converters.convert(image, r, new FloatType()),
			Converters.convert(image, g, new FloatType()),
			Converters.convert(image, b, new FloatType()));
	}

	protected List<RandomAccessible<FloatType>> processRealType(
		RandomAccessible<FloatType> image)
	{
		// TODO check number of channels
		int colorAxis = globals.numDimensions();
		if (image.numDimensions() != colorAxis + 1)
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() +
				" dimensions plus one color channel.");
		return Arrays.asList(
			sliceAndScale(image, colorAxis, 0),
			sliceAndScale(image, colorAxis, 1),
			sliceAndScale(image, colorAxis, 2));
	}

	private RandomAccessible<FloatType> sliceAndScale(
		RandomAccessible<FloatType> image, int colorAxis, int pos)
	{
		MixedTransformView<FloatType> slice = Views.hyperSlice(image, colorAxis, pos);
		Converter<FloatType, FloatType> scale = (in, out) -> out.setReal(in.getRealFloat() / 255.f);
		return Converters.convert(slice, scale, new FloatType());
	}
}
