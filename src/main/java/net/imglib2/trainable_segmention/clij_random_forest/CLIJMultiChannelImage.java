package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.imglib2.FinalInterval.createMinSize;

public class CLIJMultiChannelImage implements AutoCloseable {

	private final CLIJ2 clij;
	private final long numChannels;
	private final long[] spatialDimensions;
	private final ClearCLBuffer buffer;

	public CLIJMultiChannelImage(CLIJ2 clij, long[] spatialDimensions, long numChannels) {
		assert spatialDimensions.length >= 2 && spatialDimensions.length <= 3;
		this.clij = clij;
		this.spatialDimensions = spatialDimensions;
		this.numChannels = numChannels;
		long[] size = spatialDimensions.clone();
		size[size.length - 1] = size[size.length - 1] * numChannels;
		this.buffer = clij.create(size);
	}

	public CLIJMultiChannelImage(CLIJ2 clij, RandomAccessibleInterval<?> input) {
		long[] allDimensions = Intervals.dimensionsAsLongArray(input);
		this.clij = clij;
		this.spatialDimensions = Arrays.copyOf(allDimensions, allDimensions.length - 1);
		this.numChannels = allDimensions[allDimensions.length - 1];
		this.buffer = clij.push(input);
	}

	public List<CLIJView> channels() {
		return channelIntervals().stream().map(i -> CLIJView.interval(buffer, i)).collect(Collectors.toList());
	}

	public RandomAccessibleInterval<FloatType> asRAI() {
		RandomAccessibleInterval<FloatType> rai = clij.pullRAI(buffer);
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

	public ClearCLBuffer asClearCLBuffer() {
		return buffer;
	}

	public long[] getSpatialDimensions() {
		return spatialDimensions;
	}

	public long getNChannels() {
		return numChannels;
	}

	public void copyTo(RandomAccessibleInterval<FloatType> image) {
		if(image instanceof ArrayImg) {
			throw new UnsupportedOperationException("FIXME: implement fast copy to array img.");
//			 FIXME
//			ArrayDataAccess access = (ArrayDataAccess) (((ArrayImg) image).update(null));
//			float[] array = (float[]) access.getCurrentStorageArray();
//			FloatBuffer floatBuffer = FloatBuffer.wrap(array);
//			buffer.writeTo(floatBuffer, true);
		}
		else {
			RealTypeConverters.copyFromTo(Views.zeroMin(asRAI()), Views.zeroMin(image));
		}
	}
}
