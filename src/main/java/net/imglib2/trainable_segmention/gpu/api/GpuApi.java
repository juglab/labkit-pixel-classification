
package net.imglib2.trainable_segmention.gpu.api;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.trainable_segmention.RevampUtils;
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
			return Private.internalPullRai(image, RevampUtils.extend(image.getDimensions(), image
				.getNumberOfChannels()));
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
