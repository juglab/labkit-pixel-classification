
package net.imglib2.trainable_segmention;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthias Arzt
 */
public class RevampUtilsTest {

	@Test
	public void testGaussRequiredInput() {
		double[] sigmas = { 5.0, 2.0 };
		Interval output = new FinalInterval(new long[] { 2, 5 }, new long[] { 9, 10 });
		Interval input = RevampUtils.gaussRequiredInput(output, sigmas);
		testRequiredInput(output, input, (i, o) -> RevampUtils.gauss(Utils.ops(), i, o, sigmas));
	}

	@Test
	public void TestDeriveXRequiredInput() {
		Interval output = new FinalInterval(new long[] { 2, 5 }, new long[] { 9, 10 });
		Interval input = RevampUtils.deriveXRequiredInput(output);
		testRequiredInput(output, input, (in, out) -> RevampUtils.deriveX(Utils.ops(), in, out));
	}

	@Test
	public void TestDeriveYRequiredInput() {
		Interval output = new FinalInterval(new long[] { 2, 5 }, new long[] { 9, 10 });
		Interval input = RevampUtils.deriveYRequiredInput(output);
		testRequiredInput(output, input, (in, out) -> RevampUtils.deriveY(Utils.ops(), in, out));
	}

	private void testRequiredInput(Interval outputInterval, Interval inputInterval,
		BiConsumer<RandomAccessible<FloatType>, Interval> operation)
	{
		Img<ByteType> inputAccessed = Utils.ops().create().img(inputInterval, new ByteType());
		RandomAccessible<FloatType> input = recordAccessView(inputAccessed);
		operation.accept(input, outputInterval);
		inputAccessed.forEach(x -> assertEquals(1, x.get()));
	}

	/**
	 * Initially all pixels of "accessed" are set to zero. For each read access to a
	 * pixel of the returned {@link RandomAccessible<FloatType>}, the according
	 * pixel of "accessed" is set to one.
	 */
	private static RandomAccessible<FloatType> recordAccessView(
		Img<? extends NumericType<?>> accessed)
	{
		accessed.forEach(NumericType::setZero);
		return Converters.convert(
			(RandomAccessible<? extends NumericType<?>>) accessed,
			(in, out) -> {
				in.setOne();
				out.setZero();
			}, new FloatType());
	}

	@Test
	public void testIntervalRemoveDimension() {
		Interval input = Intervals.createMinMax(1, 2, 3, 4, 5, 6, 7, 8);
		Interval result = RevampUtils.intervalRemoveDimension(input, 1);
		assertTrue(Intervals.equals(Intervals.createMinMax(1, 3, 4, 5, 7, 8), result));
	}
}
