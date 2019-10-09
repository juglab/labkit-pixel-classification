
package preview.net.imglib2.algorithm.neighborhood;

import net.imglib2.AbstractInterval;
import net.imglib2.Cursor;
import net.imglib2.FlatIterationOrder;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;

import java.util.Iterator;

public final class NeighborhoodIterableInterval<T> extends AbstractInterval implements
	IterableInterval<Neighborhood<T>>
{

	private final RandomAccessibleInterval<T> source;

	private final long size;

	final NeighborhoodFactory factory;

	public NeighborhoodIterableInterval(final RandomAccessibleInterval<T> source,
		final NeighborhoodFactory factory)
	{
		super(source);
		this.source = source;
		this.factory = factory;

		long s = source.dimension(0);
		for (int d = 1; d < n; ++d)
			s *= source.dimension(d);
		size = s;
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public Neighborhood<T> firstElement() {
		return cursor().next();
	}

	@Override
	public Object iterationOrder() {
		return new FlatIterationOrder(this);
	}

	@Override
	public Iterator<Neighborhood<T>> iterator() {
		return cursor();
	}

	@Override
	public Cursor<Neighborhood<T>> cursor() {
		return new NeighborhoodCursor<T>(source, factory);
	}

	@Override
	public Cursor<Neighborhood<T>> localizingCursor() {
		return cursor();
	}
}
