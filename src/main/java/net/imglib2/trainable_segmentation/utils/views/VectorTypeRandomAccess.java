package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;
import net.imglib2.type.numeric.RealType;

public class VectorTypeRandomAccess implements RandomAccess<VectorType> {

	private final RandomAccess<? extends RealType<?> > ra;

	private final int n;

	private final VectorType type;

	public VectorTypeRandomAccess(RandomAccess< ? extends RealType< ? > > ra, int length) {
		this.ra = ra;
		this.n = ra.numDimensions() - 1;
		this.type = new DefaultVectorType(ra, length, n);
	}

	@Override
	public RandomAccess< VectorType > copyRandomAccess() {
		return new VectorTypeRandomAccess(ra.copyRandomAccess(), type.buffer().length);
	}

	@Override
	public long getLongPosition(int d) {
		return ra.getLongPosition(d);
	}

	@Override
	public void fwd(int d) {
		ra.fwd(d);
	}

	@Override
	public void bck(int d) {
		ra.bck(d);
	}

	@Override
	public void move(int distance, int d) {
		ra.move(distance, d);
	}

	@Override
	public void move(long distance, int d) {
		ra.move(distance, d);
	}

	@Override
	public void move(Localizable distance) {
		for (int d = 0; d < n; d++) move(distance.getLongPosition(d), d);
	}

	@Override
	public void move(int[] distance) {
		for (int d = 0; d < n; d++) move(distance[d], d);

	}

	@Override
	public void move(long[] distance) {
		for (int d = 0; d < n; d++) move(distance[d], d);
	}

	@Override
	public void setPosition(Localizable position) {
		for (int d = 0; d < n; d++) setPosition(position.getLongPosition(d), d);
	}

	@Override
	public void setPosition(int[] position) {
		for (int d = 0; d < n; d++) setPosition(position[d], d);
	}

	@Override
	public void setPosition(long[] position) {
		for (int d = 0; d < n; d++) setPosition(position[d], d);
	}

	@Override
	public void setPosition(int position, int d) {
		ra.setPosition(position, d);
	}

	@Override
	public void setPosition(long position, int d) {
		ra.setPosition(position, d);
	}

	@Override
	public int numDimensions() {
		return n;
	}

	@Override
	public VectorType get() {
		return type;
	}

	@Override
	public Sampler< VectorType > copy() {
		return copyRandomAccess();
	}
}
