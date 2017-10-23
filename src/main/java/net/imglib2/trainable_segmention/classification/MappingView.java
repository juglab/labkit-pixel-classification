package net.imglib2.trainable_segmention.classification;

import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.AbstractConvertedRandomAccess;
import net.imglib2.converter.AbstractConvertedRandomAccessible;

public class MappingView<I, O> extends AbstractConvertedRandomAccessible< I, O >
{

	final RandomAccessible<? extends I> source;

	private final UnaryFunctionOp<I, O> mapper;

	public MappingView(final RandomAccessible<? extends I> source, UnaryFunctionOp<I, O> mapper)
	{
		super( cast(source) );
		this.source = source;
		this.mapper = mapper;
	}

	private static <T,R> R cast(T in) {
		@SuppressWarnings("unchecked") R out = (R) in;
		return out;
	}

	@Override
	public InstanceAccess<I, O> randomAccess() {
		return new InstanceAccess<>( source.randomAccess(), mapper);
	}

	@Override
	public InstanceAccess<I, O> randomAccess(final Interval interval )
	{
		return randomAccess();
	}

	public static class InstanceAccess<I, O> extends AbstractConvertedRandomAccess< I, O >
	{

		private final UnaryFunctionOp<I, O> mapper;

		public InstanceAccess(final RandomAccess<? extends I> source, UnaryFunctionOp<I, O> mapper)
		{
			super( cast(source) );
			this.mapper = mapper.getIndependentInstance();
		}

		@Override
		public O get()
		{
			return mapper.calculate(source.get());
		}

		@Override
		public InstanceAccess< I, O > copy()
		{
			return new InstanceAccess<>( source.copyRandomAccess(), mapper);
		}

	}
}
