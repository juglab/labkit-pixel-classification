
package net.imglib2.trainable_segmention.clij_random_forest;

import clij.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import clij.GpuApi;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.imglib2.FinalInterval.createMinSize;

public class CLIJMultiChannelImage implements AutoCloseable {

	private final GpuApi gpu;
	private final long numChannels;
	private final long[] spatialDimensions;
	private final GpuImage buffer;

	public CLIJMultiChannelImage(GpuApi gpu, long[] spatialDimensions, long numChannels) {
		assert spatialDimensions.length >= 2 && spatialDimensions.length <= 3;
		this.gpu = gpu;
		this.spatialDimensions = spatialDimensions;
		this.numChannels = numChannels;
		long[] size = spatialDimensions.clone();
		size[size.length - 1] = size[size.length - 1] * numChannels;
		this.buffer = gpu.create(size, NativeTypeEnum.Float);
	}

	public CLIJMultiChannelImage(GpuApi gpu, RandomAccessibleInterval<? extends RealType<?>> input,
		long numChannels)
	{
		this.gpu = gpu;
		this.spatialDimensions = Intervals.dimensionsAsLongArray(input);
		spatialDimensions[spatialDimensions.length - 1] /= numChannels;
		this.numChannels = numChannels;
		this.buffer = gpu.push(input);
	}

	public List<GpuView> channels() {
		return channelIntervals().stream().map(i -> GpuView.interval(buffer, i)).collect(Collectors
			.toList());
	}

	public RandomAccessibleInterval<FloatType> asRAI() {
		RandomAccessibleInterval<FloatType> rai = gpu.pullRAI(buffer);
		List<RandomAccessibleInterval<FloatType>> slices = channelIntervals().stream()
			.map(i -> Views.zeroMin(Views.interval(rai, i)))
			.collect(Collectors.toList());
		return Views.stack(slices);
	}

	private List<Interval> channelIntervals() {
		return IntStream.range(0, (int) numChannels).mapToObj(i -> {
			int n = spatialDimensions.length;
			long[] min = new long[n];
			min[n - 1] = spatialDimensions[n - 1] * i;
			return createMinSize(min, spatialDimensions);
		}).collect(Collectors.toList());
	}

	@Override
	public void close() {
		buffer.close();
	}

	public GpuImage asClearCLBuffer() {
		return buffer;
	}

	public long[] getSpatialDimensions() {
		return spatialDimensions;
	}

	public long getNChannels() {
		return numChannels;
	}

	public void copyTo(RandomAccessibleInterval<? extends RealType<?>> image) {
		Optional<float[]> floatArray = getUnderlyingFloatArray(image);
		if (floatArray.isPresent()) {
			FloatBuffer floatBuffer = FloatBuffer.wrap(floatArray.get());
			buffer.writeTo(floatBuffer, true);
		}
		else {
			RealTypeConverters.copyFromTo(Views.zeroMin(asRAI()), Views.zeroMin(image));
		}
	}

	private Optional<float[]> getUnderlyingFloatArray(
		RandomAccessibleInterval<? extends RealType<?>> image)
	{
		if (!(image instanceof ArrayImg))
			return Optional.empty();
		Object access = ((ArrayImg) image).update(null);
		if (!(image instanceof ArrayDataAccess))
			return Optional.empty();
		ArrayDataAccess<?> arrayDataAccess = (ArrayDataAccess<?>) access;
		Object array = arrayDataAccess.getCurrentStorageArray();
		if (!(array instanceof float[]))
			return Optional.empty();
		return Optional.of((float[]) array);
	}
}
