
package net.imglib2.trainable_segmention;

import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.cached.CachedOpEnvironment;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.cache.CacheService;

import java.util.Objects;

import static org.junit.Assert.assertSame;

/**
 * Test if {@link CachedOpEnvironment} is suitable to cache once calculated
 * features like gaussian etc.
 *
 * @author Matthias
 */
public class CachedOpEnvironmentTest {

	@Ignore("CachedOpEnvironment is not working yet.")
	@Test
	public void test() {
		Context context = new Context(OpService.class, CacheService.class);
		OpService ops = context.service(OpService.class);
		CachedOpEnvironment cachedOps = new CachedOpEnvironment(ops);
		Img<FloatType> img = ImagePlusAdapter.convertFloat(Utils.loadImage("nuclei.tif"));

		UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> func =
			Functions.unary(cachedOps, Ops.Filter.Gauss.class, RandomAccessibleInterval.class,
				RandomAccessibleInterval.class, new double[] { 8, 8 });

		RandomAccessibleInterval<FloatType> result1 = func.calculate(img);

		RandomAccessibleInterval<FloatType> result2 = func.calculate(img);

		assertSame(result1, result2);
	}

	@Test
	public void testHashOfNull() {
		Objects.hashCode(null);
	}
}
