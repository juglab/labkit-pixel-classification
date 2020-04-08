
package net.imglib2.trainable_segmention.gpu.api;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

import java.util.Arrays;
import java.util.HashMap;

public class GpuApi implements AutoCloseable {

	private final CLIJ2 clij;

	private final ClearCLBufferPool pool;

	public GpuApi(CLIJ2 clij) {
		this.clij = clij;
		this.pool = new ClearCLBufferPool(clij.getCLIJ().getClearCLContext());
	}

	public GpuImage create(long[] dimensions, NativeTypeEnum type) {
		return create(dimensions, 1, type);
	}

	public GpuImage create(long[] dimensions, long numberOfChannels, NativeTypeEnum type) {
		return new GpuImage(pool.create(dimensions, numberOfChannels, type), pool::release);
	}

	public GpuImage push(RandomAccessibleInterval<? extends RealType<?>> source) {
		GpuImage target = create(Intervals.dimensionsAsLongArray(source), getNativeTypeEnum(source));
		GpuCopy.copyFromTo(source, target);
		return target;
	}

	public GpuImage pushMultiChannel(RandomAccessibleInterval<? extends RealType<?>> input) {
		long[] dimensions = Intervals.dimensionsAsLongArray(input);
		int n = dimensions.length - 1;
		GpuImage buffer = create(Arrays.copyOf(dimensions, n), dimensions[n], NativeTypeEnum.Float);
		GpuCopy.copyFromTo(input, buffer);
		return buffer;
	}

	public <T extends RealType<?>> RandomAccessibleInterval<T> pullRAI(GpuImage image) {
		if (image.getNumberOfChannels() > 1)
			return pullRAIMultiChannel(image);
		return internalPullRai(image, image.getDimensions());
	}

	public <T extends RealType<?>> RandomAccessibleInterval<T> pullRAIMultiChannel(GpuImage image) {
		return internalPullRai(image, RevampUtils.extend(image.getDimensions(), image
			.getNumberOfChannels()));
	}

	private NativeTypeEnum getNativeTypeEnum(RandomAccessibleInterval<? extends RealType> image) {
		RealType type = Util.getTypeFromInterval(image);
		if (type instanceof FloatType)
			return NativeTypeEnum.Float;
		if (type instanceof UnsignedShortType)
			return NativeTypeEnum.UnsignedShort;
		throw new UnsupportedOperationException();
	}

	private <T extends RealType<?>> RandomAccessibleInterval<T> internalPullRai(GpuImage source,
		long[] dimensions)
	{
		RealType<?> type = GpuCopy.getImgLib2Type(source.getNativeType());
		Img<T> target = Cast.unchecked(new ArrayImgFactory<>(Cast.unchecked(type)).create(dimensions));
		GpuCopy.copyFromTo(source, target);
		return target;
	}

	@Override
	public void close() {
		pool.close();
	}

	public static GpuApi getInstance() {
		CLIJ2 clij = CLIJ2.getInstance();
		clij.setKeepReferences(false);
		return new GpuApi(clij);
	}

	public void execute(Class<?> anchorClass, String kernelFile, String kernelName,
		long[] globalSizes, long[] localSizes, HashMap<String, Object> parameters,
		HashMap<String, Object> defines)
	{
		for (String key : parameters.keySet()) {
			Object value = parameters.get(key);
			if (value instanceof GpuImage)
				parameters.put(key, ((GpuImage) value).clearCLBuffer());
		}
		clij.executeSubsequently(anchorClass, kernelFile, kernelName, null, globalSizes, localSizes,
			parameters, defines, null).close();
	}

	public void execute(Class<?> anchorClass, String kernelFile, String kernelName,
		long[] globalSizes, HashMap<String, Object> parameters, HashMap<String, Object> constants)
	{
		execute(anchorClass, kernelFile, kernelName, globalSizes, null, parameters, constants);
	}

	public void minimum2DBox(GpuImage inputBuffer, GpuImage tmp, long l, long l1) {
		clij.minimum2DBox(inputBuffer.clearCLBuffer(), tmp.clearCLBuffer(), l, l1);
	}

	public void minimum3DBox(GpuImage inputBuffer, GpuImage tmp, long l, long l1, long l2) {
		clij.minimum3DBox(inputBuffer.clearCLBuffer(), tmp.clearCLBuffer(), l, l1, l2);
	}

	public void maximum2DBox(GpuImage inputBuffer, GpuImage tmp, long l, long l1) {
		clij.maximum2DBox(inputBuffer.clearCLBuffer(), tmp.clearCLBuffer(), l, l1);
	}

	public void maximum3DBox(GpuImage inputBuffer, GpuImage tmp, long l, long l1, long l2) {
		clij.maximum3DBox(inputBuffer.clearCLBuffer(), tmp.clearCLBuffer(), l, l1, l2);
	}

	public void mean2DBox(GpuImage inputBuffer, GpuImage tmp, long l, long l1) {
		clij.mean2DBox(inputBuffer.clearCLBuffer(), tmp.clearCLBuffer(), l, l1);
	}

	public void mean3DBox(GpuImage inputBuffer, GpuImage tmp, long l, long l1, long l2) {
		clij.mean3DBox(inputBuffer.clearCLBuffer(), tmp.clearCLBuffer(), l, l1, l2);
	}

	@Deprecated
	public void gaussianBlur3D(GpuImage inputBuffer, GpuImage output, int i, int i1, int i2) {
		clij.gaussianBlur3D(inputBuffer.clearCLBuffer(), output.clearCLBuffer(), i, i1, i2);
	}
}
