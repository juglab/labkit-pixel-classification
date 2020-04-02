
package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

public class GaussTest {

	@Test
	public void test() {
		CLIJ2 clij = CLIJ2.getInstance();
		RandomAccessible<FloatType> dirac = Utils.dirac(3);
		RandomAccessibleInterval<FloatType> expected = Utils.create3dImage(Intervals.createMinMax(-2,
			-2, -2, 2, 2, 2),
			(x, y, z) -> Utils.gauss(2, x, y, z));
		Interval targetInterval = new FinalInterval(expected);
		NeighborhoodOperation operation = Gauss.gauss(clij, 2, 2, 2);
		Interval inputInterval = operation.getRequiredInputInterval(targetInterval);
		try (
			ClearCLBuffer input = clij.push(Views.interval(dirac, inputInterval));
			ClearCLBuffer output = clij.create(Intervals.dimensionsAsLongArray(targetInterval),
				NativeTypeEnum.Float);)
		{
			operation.convolve(CLIJView.wrap(input), CLIJView.wrap(output));
			RandomAccessibleInterval<FloatType> rai = clij.pullRAI(output);
			ImgLib2Assert.assertImageEqualsRealType(Views.zeroMin(expected), rai, 1.e-7);
		}
	}
}
