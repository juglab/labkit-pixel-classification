package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;
import net.imglib2.util.Cast;
import net.imglib2.view.composite.Composite;
import preview.net.imglib2.loops.ClassCopyProvider;

import java.util.Arrays;

public class VectorRandomAccess< T> implements RandomAccess< Composite< T >>,
		Composite< T >
{

	private static final ClassCopyProvider<RandomAccess> provider =
			new ClassCopyProvider<>(VectorRandomAccess.class, RandomAccess.class);

	public static <T> RandomAccess<Composite<T>> create(RandomAccess<T>... randomAccesses) {
		Object key = Arrays.asList(randomAccesses.length, randomAccesses[0].getClass());
		return provider.newInstanceForKey(key, new Object[]{randomAccesses});
	}

	RandomAccess<T> ras[];

	public VectorRandomAccess(RandomAccess<T>... randomAccesses) {
		this.ras = randomAccesses;
	}

	@Override
	public RandomAccess< Composite< T > > copyRandomAccess() {
		RandomAccess[] copy = new RandomAccess[ras.length];
		for (int i = 0; i < copy.length; i++)
			copy[i]	= ras[i].copyRandomAccess();
		return new VectorRandomAccess<>(Cast.unchecked(copy));
	}

	@Override
	public long getLongPosition(int d) {
		return 0;
	}

	@Override
	public void fwd(int d) {
		for(RandomAccess<T> ra : ras)
			ra.fwd(d);
	}

	@Override
	public void bck(int d) {
		for(RandomAccess<T> ra : ras)
			ra.bck(d);
	}

	@Override
	public void move(int distance, int d) {
		for(RandomAccess<T> ra : ras)
			ra.move(distance, d);
	}

	@Override
	public void move(long distance, int d) {
		for(RandomAccess<T> ra : ras)
			ra.move(distance, d);
	}

	@Override
	public void move(Localizable distance) {
		for(RandomAccess<T> ra : ras)
			ra.move(distance);
	}

	@Override
	public void move(int[] distance) {
		for(RandomAccess<T> ra : ras)
			ra.move(distance);
	}

	@Override
	public void move(long[] distance) {
		for(RandomAccess<T> ra : ras)
			ra.move(distance);
	}

	@Override
	public void setPosition(Localizable position) {
		for(RandomAccess<T> ra : ras)
			ra.setPosition(position);
	}

	@Override
	public void setPosition(int[] position) {
		for(RandomAccess<T> ra : ras)
			ra.setPosition(position);
	}

	@Override
	public void setPosition(long[] position) {
		for(RandomAccess<T> ra : ras)
			ra.setPosition(position);
	}

	@Override
	public void setPosition(int position, int d) {
		for(RandomAccess<T> ra : ras)
			ra.setPosition(position, d);
	}

	@Override
	public void setPosition(long position, int d) {
		for(RandomAccess<T> ra : ras)
			ra.setPosition(position, d);
	}

	@Override
	public int numDimensions() {
		return ras[0].numDimensions();
	}

	@Override
	public Composite< T > get() {
		return this;
	}

	@Override
	public Sampler< Composite< T > > copy() {
		return copyRandomAccess();
	}

	@Override
	public T get(long i) {
		return ras[(int) i].get();
	}
}
