
package clij;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.utils.ToString;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;
import preview.net.imglib2.algorithm.convolution.kernel.Kernel1D;

import java.util.Arrays;

/**
 * Tests {@link ConcatenatedNeighborhoodOperation}.
 */
public class ConcatenatedNeighborhoodOperationTest {

	@Test
	public void test() {
		CLIJ2 clij = CLIJ2.getInstance();
		Img<FloatType> dirac = ArrayImgs.floats(new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0
		}, 5, 5);
		CLIJView input = CLIJView.wrap(clij.push(dirac));
		CLIJView output = CLIJView.wrap(clij.create(new long[] { 3, 3 }, NativeTypeEnum.Float));
		CLIJKernelConvolution a = new CLIJKernelConvolution(clij, Kernel1D.centralAsymmetric(1, 0, -1),
			0);
		CLIJKernelConvolution b = new CLIJKernelConvolution(clij, Kernel1D.centralAsymmetric(1, 2, 1),
			1);
		ConcatenatedNeighborhoodOperation concatenation = new ConcatenatedNeighborhoodOperation(clij,
			Arrays.asList(a, b));
		Interval inputInterval = concatenation.getRequiredInputInterval(Intervals.createMinMax(-1, -1,
			1, 1));
		Interval expectedInterval = Intervals.createMinMax(-2, -2, 2, 2);
		ImgLib2Assert.assertIntervalEquals(expectedInterval, inputInterval);
		concatenation.convolve(input, output);
		RandomAccessibleInterval<FloatType> rai = clij.pullRAI(output.buffer());
		ToString.print(rai);
	}
}
