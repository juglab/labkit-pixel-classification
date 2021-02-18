package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

public final class WrappedRandomAccess<T> implements RandomAccess< RandomAccess< T > > {

	public static <T> RandomAccessibleInterval< RandomAccess< T > > wrap(RandomAccessibleInterval< T > image) {
		return SimpleRAI.create(new WrappedRandomAccess<>(image.randomAccess()),
				Intervals.hyperSlice(image, image.numDimensions() - 1));
	}

	private final RandomAccess< T > ra;

	private final int n;

	public WrappedRandomAccess(RandomAccess< T > ra) {
		this.ra = ra;
		this.n = ra.numDimensions() - 1;
	}

	@Override
	public RandomAccess< RandomAccess< T > > copyRandomAccess() {
		return new WrappedRandomAccess(ra.copyRandomAccess());
	}

	private final Child child = new Child();

	@Override
	public RandomAccess< T > get() {
		return child;
	}

	@Override
	public Sampler< RandomAccess< T > > copy() {
		return copyRandomAccess();
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

	public class Child implements RandomAccess< T > {

		@Override
		public RandomAccess< T > copyRandomAccess() {
			return null;
		}

		@Override
		public long getLongPosition(int d) {
			return 0;
		}

		@Override
		public void fwd(int d) {
			ra.fwd(n);
		}

		@Override
		public void bck(int d) {

		}

		@Override
		public void move(int distance, int d) {

		}

		@Override
		public void move(long distance, int d) {

		}

		@Override
		public void move(Localizable distance) {

		}

		@Override
		public void move(int[] distance) {

		}

		@Override
		public void move(long[] distance) {

		}

		@Override
		public void setPosition(Localizable position) {

		}

		@Override
		public void setPosition(int[] position) {

		}

		@Override
		public void setPosition(long[] position) {

		}

		@Override
		public void setPosition(int position, int d) {
			ra.setPosition(position, n);
		}

		@Override
		public void setPosition(long position, int d) {
			ra.setPosition(position, n);
		}

		@Override
		public int numDimensions() {
			return 0;
		}

		@Override
		public T get() {
			return ra.get();
		}

		@Override
		public Sampler< T > copy() {
			return null;
		}
	}
}
