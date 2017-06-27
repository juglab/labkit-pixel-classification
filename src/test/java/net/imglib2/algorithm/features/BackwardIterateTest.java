package net.imglib2.algorithm.features;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Tests {@link BackwardIterate}
 *
 * @author Matthias Arzt
 */
public class BackwardIterateTest {

	@Test
	public void test() {

		long[] max = {10, 20, 5};
		long[] min = {8, 2, 3};

		Interval interval = new FinalInterval(min, max);
		Iterator<Localizable> iterator = BackwardIterate.iterable(interval).iterator();

		for(long z = max[2]; z >= min[2]; z--) {
			for(long y = max[1]; y >= min[1]; y--) {
				for(long x = max[0]; x >= min[0]; x--) {
					assertTrue(iterator.hasNext());
					Localizable localizable = iterator.next();
					assertEquals(new Point(x, y, z), localizable);
				}
			}
		}
		assertFalse(iterator.hasNext());
	}
}