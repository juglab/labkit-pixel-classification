package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;

import java.util.Arrays;

public class DefaultVectorType implements VectorType {

	private final RandomAccess<? extends RealType<?> > ra;

	private final float[] buffer;

	private final int d;

	public DefaultVectorType(RandomAccess< ? extends RealType< ? > > ra, int length, int d) {
		this.ra = ra;
		this.buffer = new float[length];
		this.d = d;
	}

	@Override
	public float[] buffer() {
		return buffer;
	}

	@Override
	public float[] get() {
		for (int i = 0; i < buffer.length; i++) {
			ra.setPosition(i, d);
			buffer[i] = ra.get().getRealFloat();
		}
		return buffer;
	}

	@Override
	public void set(float[] values) {
		for (int i = 0; i < buffer.length; i++) {
			ra.setPosition(i, d);
			ra.get().setReal(values[i]);
		}
	}

	@Override
	public DefaultVectorType createVariable() {
		return new DefaultVectorType(ArrayImgs.floats(buffer.length).randomAccess(),
				buffer.length, d);
	}

	@Override
	public VectorType copy() {
		return new DefaultVectorType(ra.copyRandomAccess(), buffer.length, d);
	}

	@Override
	public void set(VectorType c) {
		set(c.get());
	}

	@Override
	public boolean valueEquals(VectorType other) {
		return Arrays.equals(this.get(), other.get());
	}
}
