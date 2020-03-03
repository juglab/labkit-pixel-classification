
package net.imglib2.trainable_segmention.clij_random_forest;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class CLIJCopy {

	public static void copy3dStack(CLIJ2 clij, ClearCLBuffer input, ClearCLBuffer output, int offset,
		int step)
	{
		long[] globalSizes = input.getDimensions();
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src", input);
		parameters.put("dst", output);
		parameters.put("offset", offset);
		parameters.put("step", step);
		clij.execute(CLIJCopy.class, "copy_3d_stack.cl", "copy_3d_stack", globalSizes, globalSizes,
			parameters);
	}

	public static void copy(CLIJ2 clij, CLIJView src, CLIJView dst) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src_offset_x", min(src, 0));
		parameters.put("src_offset_y", min(src, 1));
		parameters.put("src_offset_z", min(src, 2));
		parameters.put("dst_offset_x", min(dst, 0));
		parameters.put("dst_offset_y", min(dst, 1));
		parameters.put("dst_offset_z", min(dst, 2));
		parameters.put("src", src.buffer());
		parameters.put("dst", dst.buffer());
		long[] globalSizes = Intervals.dimensionsAsLongArray(src.interval());
		if (!Intervals.equalDimensions(src.interval(), dst.interval()))
			throw new IllegalArgumentException();
		clij.execute(CLIJCopy.class, "copy_with_offset.cl", "copy_with_offset", globalSizes,
			globalSizes,
			parameters);
	}

	private static int min(CLIJView view, int d) {
		Interval interval = view.interval();
		return (int) (d < interval.numDimensions() ? interval.min(d) : 0);
	}

	public static void copyToRai(ClearCLBuffer source,
		RandomAccessibleInterval<? extends RealType<?>> target)
	{
		if (!Arrays.equals(source.getDimensions(), Intervals.dimensionsAsLongArray(target)))
			throw new IllegalArgumentException("Dimensions don't match.");
		RealType<?> sourceType = getImgLib2Type(source.getNativeType());
		RealType<?> targetType = Util.getTypeFromInterval(target);
		Optional<Object> optionalArray = getBackingArray(target);
		if (optionalArray.isPresent() && sourceType.getClass() == targetType.getClass()) {
			Object array = optionalArray.get();
			Buffer buffer = wrapAsBuffer(array);
			source.writeTo(buffer, true);
			return;
		}
		RandomAccessibleInterval<RealType<?>> tmp = new ArrayImgFactory<>((NativeType) sourceType)
			.create(source.getDimensions());
		copyToRai(source, tmp);
		RealTypeConverters.copyFromTo(tmp, Views.zeroMin(target));
	}

	private static RealType<?> getImgLib2Type(NativeTypeEnum nativeType) {
		switch (nativeType) {
			case Byte:
				return new ByteType();
			case UnsignedByte:
				return new UnsignedByteType();
			case Short:
				return new ShortType();
			case UnsignedShort:
				return new UnsignedShortType();
			case Int:
				return new IntType();
			case UnsignedInt:
				return new UnsignedIntType();
			case Long:
				return new LongType();
			case UnsignedLong:
				return new UnsignedLongType();
			case HalfFloat:
				throw new UnsupportedOperationException();
			case Float:
				return new FloatType();
			case Double:
				return new DoubleType();
		}
		throw new UnsupportedOperationException();
	}

	private static Buffer wrapAsBuffer(Object array) {
		if (array instanceof byte[])
			return ByteBuffer.wrap((byte[]) array);
		if (array instanceof float[])
			return FloatBuffer.wrap((float[]) array);
		if (array instanceof short[])
			return ShortBuffer.wrap((short[]) array);
		if (array instanceof int[])
			return IntBuffer.wrap((int[]) array);
		throw new UnsupportedOperationException();
	}

	private static Optional<Object> getBackingArray(RandomAccessibleInterval<?> target) {
		if (target instanceof ArrayImg)
			return getBackingArray((ArrayImg<?, ?>) target);
		return Optional.empty();
	}

	private static Optional<Object> getBackingArray(ArrayImg<?, ?> image) {
		Object access = image.update(null);
		if (access instanceof ArrayDataAccess)
			return Optional.of(((ArrayDataAccess) access).getCurrentStorageArray());
		return Optional.empty();
	}
}
