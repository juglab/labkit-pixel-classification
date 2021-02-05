package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.type.Type;

public interface VectorType extends Type<VectorType > {

	float[] buffer();

	float[] get();

	void set(float[] values);

	@Override
	VectorType createVariable();

	@Override
	VectorType copy();

	@Override
	void set(VectorType c);

	@Override
	boolean valueEquals(VectorType other);
}
