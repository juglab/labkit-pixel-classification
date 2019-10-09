
package preview.net.imglib2.algorithm.neighborhood;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;

public final class NeighborhoodRandomAccessible<T> extends AbstractEuclideanSpace implements
	RandomAccessible<Neighborhood<T>>
{

	private final RandomAccessible<T> source;

	private final NeighborhoodFactory factory;

	public NeighborhoodRandomAccessible(final RandomAccessible<T> source,
		final NeighborhoodFactory factory)
	{
		super(source.numDimensions());
		this.source = source;
		this.factory = factory;
	}

	@Override
	public RandomAccess<Neighborhood<T>> randomAccess() {
		return new NeighborhoodRandomAccess<T>(source, factory);
	}

	@Override
	public RandomAccess<Neighborhood<T>> randomAccess(final Interval interval) {
		return new NeighborhoodRandomAccess<T>(source, factory, interval);
	}
}
