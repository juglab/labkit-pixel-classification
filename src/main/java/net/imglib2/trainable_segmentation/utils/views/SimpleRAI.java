package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

public class SimpleRAI<T> implements RandomAccessible<T> {

	private final RandomAccess< T > blueprint;

	public static < T > RandomAccessibleInterval< T > create( RandomAccess< T > randomAccess, Interval interval ) {
		return Views.interval(new SimpleRAI<>(randomAccess), interval);
	}

	private SimpleRAI(RandomAccess< T > blueprint) {
		this.blueprint = blueprint;
	}

	@Override
	public RandomAccess< T > randomAccess() {
		return blueprint.copyRandomAccess();
	}

	@Override
	public RandomAccess< T > randomAccess(Interval interval) {
		return blueprint.copyRandomAccess();
	}

	@Override
	public int numDimensions() {
		return blueprint.numDimensions();
	}
}
