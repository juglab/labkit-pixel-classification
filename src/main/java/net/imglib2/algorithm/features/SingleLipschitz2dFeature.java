package net.imglib2.algorithm.features;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author Matthias Arzt
 */
public class SingleLipschitz2dFeature implements Feature {

	private final boolean topHat;

	private final boolean downHat;

	private final double slope;

	public SingleLipschitz2dFeature(boolean topHat, boolean downHat, double slope) {
		this.topHat = topHat;
		this.downHat = downHat;
		this.slope = slope;
	}

	@Override
	public int count() {
		return 1;
	}

	@Override
	public void apply(RandomAccessible<FloatType> in, List<RandomAccessibleInterval<FloatType>> out) {
		applySingle(in, out.get(0));
	}

	@Override
	public List<String> attributeLabels() {
		return Collections.singletonList("Lipschitz_" + topHat + "_" + downHat + "_" + slope);
	}

	private void applySingle(RandomAccessible<FloatType> in, RandomAccessibleInterval<FloatType> out) {
		// FIXME implement Lipschitz to work an RandomAccessible without interval
		// FIXME cleanup Lipschitz calculation
		RandomAccessibleInterval<FloatType> in1 = Views.interval(in, out);
		RandomAccessibleInterval<FloatType> result = RevampUtils.toFloat(calculate(RevampUtils.toInt(in1)));
		RevampUtils.copy(result, out);
	}

	RandomAccessibleInterval<IntType> calculate(RandomAccessibleInterval<IntType> input) {
		RandomAccessibleInterval<IntType> src = copy(input);
		RandomAccessibleInterval<IntType> dest = copy(input);
		process1(dest, src);
		process2(dest);
		RandomAccessibleInterval<IntType> result = copyAndMap2(dest, src);
		return result;
	}

	private RandomAccessibleInterval<IntType> copy(RandomAccessibleInterval<IntType> input) {
		Img<IntType> result = RevampUtils.ops().create().img(input);
		for(Pair<IntType, IntType> pair : Views.interval(Views.pair(result, input), input))
			pair.getA().set(pair.getB());
		return result;
	}

	private String toString(RandomAccessibleInterval<IntType> src) {
		StringJoiner joiner = new StringJoiner(", ");
		for(IntType pixel : Views.iterable(src))
			joiner.add(Integer.toString(pixel.get()));
		return "[" + joiner.toString() + "]";
	}

	private void process1(RandomAccessibleInterval<IntType> dest, RandomAccessibleInterval<IntType> src) {
		int sign = (downHat ? 1 : -1 );
		int topdown = (downHat ? 0 : 255);
		int slope = (int) (this.slope);
		int slope1 = (int) (slope * Math.sqrt(2.0));
		int p2;
		int p3;
		int max;
		int p1;
		int p4;
		RandomAccess<IntType> destRandomAccess = dest.randomAccess();
		for (long y = dest.min(1); y <= dest.max(1); y++)   // rows
		{
			p2= sign * (topdown + (sign) * slope);
			p3= sign * (topdown + (sign) * slope1);
			for (long x = dest.min(0); x <= dest.max(0); x++) // columns
			{
				max = (p2 - slope);
				p1 = (p3 - slope1);
				if (p1 > max) max = p1;
				destRandomAccess.setPosition(x, 0);
				destRandomAccess.setPosition(Math.max(y - 1, dest.min(1)), 1);
				p3 = destRandomAccess.get().get();
				p1 = p3 - slope;
				if (p1 > max) max = p1;

				destRandomAccess.setPosition(Math.min(x+1, dest.max(0)), 0);
				p4 = destRandomAccess.get().get();
				p1 = p4 - slope1;
				if (p1 > max) max = p1;

				destRandomAccess.setPosition(x, 0);
				destRandomAccess.setPosition(y, 1);
				p2 = destRandomAccess.get().get();
				if (max > p2) {
					destRandomAccess.get().set(max);
					p2 = max;
				}
			}
		}
	}

	private void process2(RandomAccessibleInterval<IntType> dest) {
		int sign = (downHat ? 1 : -1 );
		int topdown = (downHat ? 0 : 255);
		int slope = (int) (this.slope);
		int slope1 = (int) (slope * Math.sqrt(2.0));
		int p2;
		int p3;
		int max;
		int p1;
		int p4;
		RandomAccess<IntType> access = dest.randomAccess();
		for (long y = dest.max(1); y >= dest.min(1); y--)   // rows
		{
			p2= sign * (topdown + (sign) * slope);
			p3= sign * (topdown + (sign) * slope1);
			for (long x = dest.max(0); x >= dest.min(0); x--)  // columns
			{
				max = (p2 - slope);
				p1= (p3 - slope1);
				if (p1 > max) max = p1;

				access.setPosition(x, 0);
				access.setPosition(Math.min(y + 1, dest.max(1)), 1);
				p3 = access.get().get();
				p1 = p3 - slope;
				if (p1 > max)   max = p1;

				access.setPosition(Math.max(x - 1, dest.min(0)), 0);
				p4 = access.get().get();
				p1 = p4 - slope1;
				if (p1 > max) max = p1;

				access.setPosition(x, 0);
				access.setPosition(y, 1);
				p2 = access.get().get();
				if (max > p2)
				{
					access.get().set(max);
					p2 = max;
				}
			}
		}
	}

	private RandomAccessibleInterval<IntType> copyAndMap2(RandomAccessibleInterval<IntType> dest, RandomAccessible<IntType> src) {
		assert topHat;
		assert downHat;
		RandomAccessibleInterval<Pair<IntType, IntType>> paired =
			Views.interval(Views.pair(dest, src), dest);
		Converter<Pair<IntType, IntType>, IntType> converter =
			(in, out) -> out.set(in.getB().get() - in.getA().get() + 255);
		return Converters.convert(paired, converter, new IntType());
	}
}
