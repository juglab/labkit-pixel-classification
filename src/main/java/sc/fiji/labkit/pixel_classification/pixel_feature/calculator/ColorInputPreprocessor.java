/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * @author Matthias Arzt
 */
public class ColorInputPreprocessor implements InputPreprocessor {

	protected final GlobalSettings globals;

	public ColorInputPreprocessor(GlobalSettings globals) {
		this.globals = globals;
	}

	@Override
	public List<RandomAccessible<FloatType>> getChannels(RandomAccessible<?> input) {
		Object element = input.randomAccess().get();
		if (element instanceof ARGBType)
			return processARGBType(RevampUtils.uncheckedCast(input));
		else if (element instanceof RealType)
			return processRealType(RevampUtils.randomAccessibleToFloat(RevampUtils.uncheckedCast(input)));
		throw new IllegalArgumentException("Input image must be RealType or ARGBType.");
	}

	protected List<RandomAccessible<FloatType>> processARGBType(RandomAccessible<ARGBType> image) {
		if (image.numDimensions() != globals.numDimensions())
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() +
				" dimensions.");
		return Arrays.asList(
			Converters.convert(image, (in, out) -> out.set(ARGBType.red(in.get())), new FloatType()),
			Converters.convert(image, (in, out) -> out.set(ARGBType.green(in.get())), new FloatType()),
			Converters.convert(image, (in, out) -> out.set(ARGBType.blue(in.get())), new FloatType()));
	}

	protected List<RandomAccessible<FloatType>> processRealType(RandomAccessible<FloatType> image) {
		// TODO check number of channels
		int colorAxis = globals.numDimensions();
		if (image.numDimensions() != colorAxis + 1)
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() +
				" dimensions plus one color channel.");
		return Arrays.asList(
			Views.hyperSlice(image, colorAxis, 0),
			Views.hyperSlice(image, colorAxis, 1),
			Views.hyperSlice(image, colorAxis, 2));
	}

	@Override
	public Class<?> getType() {
		return ARGBType.class;
	}

	@Override
	public Interval outputIntervalFromInput(RandomAccessibleInterval<?> image) {
		Object element = image.randomAccess().get();
		if (element instanceof ARGBType)
			return image;
		else if (element instanceof RealType)
			return outputIntervalFromInputRealType(image);
		return null;
	}

	private Interval outputIntervalFromInputRealType(RandomAccessibleInterval<?> image) {
		int colorAxis = globals.numDimensions();
		if (image.numDimensions() != colorAxis + 1)
			throw new IllegalArgumentException("Input image must have " + globals.numDimensions() +
				" dimensions plus one color channel.");
		if (image.dimension(colorAxis) != 3)
			throw new IllegalArgumentException("Input image must contain three color channels.");
		return new FinalInterval(Arrays.copyOf(Intervals.minAsLongArray(image), colorAxis),
			Arrays.copyOf(Intervals.maxAsLongArray(image), colorAxis));
	}
}
