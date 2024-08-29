/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.gpu.api;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import org.apache.commons.lang3.ArrayUtils;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public interface GpuApi extends AutoCloseable {

	GpuImage create(long[] dimensions, long numberOfChannels, NativeTypeEnum type);

	GpuApi subScope();

	@Override
	void close();

	default GpuImage create(long[] dimensions, NativeTypeEnum type) {
		return create(dimensions, 1, type);
	}

	default GpuImage push(RandomAccessibleInterval<? extends RealType<?>> source) {
		GpuImage target = create(Intervals.dimensionsAsLongArray(source), GpuCopy.getNativeTypeEnum(
			source));
		return handleOutOfMemoryException(() -> {
			GpuCopy.copyFromTo(source, target);
			return target;
		});
	}

	default GpuImage pushMultiChannel(RandomAccessibleInterval<? extends RealType<?>> input) {
		long[] dimensions = Intervals.dimensionsAsLongArray(input);
		int n = dimensions.length - 1;
		GpuImage buffer = create(Arrays.copyOf(dimensions, n), dimensions[n], NativeTypeEnum.Float);
		return handleOutOfMemoryException(() -> {
			GpuCopy.copyFromTo(input, buffer);
			return buffer;
		});
	}

	default <T extends RealType<?>> RandomAccessibleInterval<T> pullRAI(GpuImage image) {
		return handleOutOfMemoryException(() -> {
			if (image.getNumberOfChannels() > 1)
				return pullRAIMultiChannel(image);
			return Private.internalPullRai(image, image.getDimensions());
		});
	}

	default <T extends RealType<?>> RandomAccessibleInterval<T> pullRAIMultiChannel(GpuImage image) {
		return handleOutOfMemoryException(() -> {
			long[] dimensions = ArrayUtils.add( image.getDimensions(), image.getNumberOfChannels() );
			return Private.internalPullRai(image, dimensions );
		});
	}

	void execute(Class<?> anchorClass, String kernelFile, String kernelName, long[] globalSizes,
		long[] localSizes, HashMap<String, Object> parameters, HashMap<String, Object> defines);

	<T> T handleOutOfMemoryException(Supplier<T> action);
}

class Private {

	private Private() {
		// prevent from instantiation
	}

	static <T extends RealType<?>> RandomAccessibleInterval<T> internalPullRai(GpuImage source,
		long[] dimensions)
	{
		RealType<?> type = GpuCopy.getImgLib2Type(source.getNativeType());
		Img<T> target = Cast.unchecked(new ArrayImgFactory<>(Cast.unchecked(type)).create(dimensions));
		GpuCopy.copyFromTo(source, target);
		return target;
	}
}
