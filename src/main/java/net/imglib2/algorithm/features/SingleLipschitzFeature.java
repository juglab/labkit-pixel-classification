package net.imglib2.algorithm.features;

import net.imglib2.*;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @author Matthias Arzt
 */
public class SingleLipschitzFeature implements Feature {

	private final double slope;

	private final long border;

	public SingleLipschitzFeature(double slope, long border) {
		this.slope = slope;
		this.border = border;
	}

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		apply(in, out.get(0));
	}

	private void apply(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		Interval expandedInterval = Intervals.expand(out, RevampUtils.nCopies(out.numDimensions(), border));
		Img<FloatType> tmp = RevampUtils.ops().create().img(expandedInterval, new FloatType());
		copy(tmp, in);
		lipschitz(tmp);
		outEquals255PlusAMinusB(Views.iterable(out), in, tmp); // out = 255 + in - tmp
	}

	private <T extends Type<T>> void copy(IterableInterval<T> out, RandomAccessible<T> in) {
		Cursor<T> o = out.cursor();
		RandomAccess<T> a = in.randomAccess();
		while(o.hasNext()) {
			o.fwd();
			a.setPosition(o);
			o.get().set(a.get());
		}
	}

	private <T extends RealType<T>> void outEquals255PlusAMinusB(IterableInterval<T> out, RandomAccessible<T> A, RandomAccessible<T> B) {
		Cursor<T> o = out.cursor();
		RandomAccess<T> a = A.randomAccess();
		RandomAccess<T> b = B.randomAccess();
		T offset = a.get().createVariable();
		offset.setReal(255);
		while(o.hasNext()) {
			o.fwd();
			a.setPosition(o);
			b.setPosition(o);
			T ov = o.get();
			ov.set(offset);
			ov.sub(b.get());
			ov.add(a.get());
		}
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Lipschitz_true_true_" + slope);
	}

	<T extends RealType<T>> void lipschitz(RandomAccessibleInterval<T> inOut) {
		int n = inOut.numDimensions();
		Interval interval = new FinalInterval(RevampUtils.nCopies(n, -1), RevampUtils.nCopies(n, 1));
		for(Localizable location : BackwardIterate.iterable(interval)) {
			if(!isZero(location))
				forward(Views.extendBorder(inOut), inOut, locationToArray(location));
		}
	}

	<T extends RealType<T>> void forward(final RandomAccessible<T> in, final RandomAccessibleInterval<T> out, final long[] translation) {
		final double slope = distance(new Point(out.numDimensions()), Point.wrap(translation)) * this.slope;
		final RandomAccessibleInterval<Pair<T, T>> pair = invert(Views.interval(Views.pair(Views.translate(in, translation), out), out), translation);
		Views.flatIterable(pair).forEach(p -> p.getB().setReal(
				Math.max(p.getB().getRealDouble(), p.getA().getRealDouble() - slope)
		));
	}

	private boolean isZero(Localizable location) {
		return locationToStream(location).allMatch(x -> x == 0);
	}

	private long[] locationToArray(Localizable cursor) {
		return locationToStream(cursor).toArray();
	}

	private LongStream locationToStream(Localizable cursor) {
		return IntStream.range(0, cursor.numDimensions()).mapToLong(cursor::getLongPosition);
	}

	private <T> RandomAccessibleInterval<T> invert(RandomAccessibleInterval<T> pair, long[] translation) {
		for(int i = pair.numDimensions() - 1; i >= 0; i--)
			if(translation[i] < 0)
				return Views.invertAxis(pair, i);
		return pair;
	}

	private static double distance(Localizable a, Localizable b) {
		int n = a.numDimensions();
		long sum = 0;
		for (int i = 0; i < n; i++) {
			long difference = a.getLongPosition(i) - b.getLongPosition(i);
			sum += difference * difference;
		}
		return Math.sqrt(sum);
	}
}
