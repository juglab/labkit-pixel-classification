/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
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
