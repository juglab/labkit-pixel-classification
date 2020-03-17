package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Ignore;
import org.junit.Test;

public class GaussTest {

	CLIJ2 clij = CLIJ2.getInstance();

	@Test
	public void test() {
		try(
				ClearCLBuffer input = clij.push(img1D(0, 0, 1, 0, 0));
				ClearCLBuffer kernel = clij.push(img1D(-0.5f, 0, 0.5f));
				ClearCLBuffer output = clij.create(new long[]{3, 1});
		) {
			Gauss.convolve(clij, input, kernel, output);
			ImgLib2Assert.assertImageEqualsRealType(img1D(0.5f, 0, -0.5f), clij.pullRAI(output), 0);
		}
	}

	@Test
	public void testBorder() {
		try(
				ClearCLBuffer input = clij.push(img1D(2, 0, 0, 0, 3));
				ClearCLBuffer kernel = clij.push(img1D(-0.5f, 0, 0.5f));
				ClearCLBuffer output = clij.create(new long[]{3, 1});
		) {
			Gauss.convolve(clij, input, kernel, output);
			ImgLib2Assert.assertImageEqualsRealType(img1D(-1, 0, 1.5f), clij.pullRAI(output), 0);
		}
	}

	@Ignore("FIXME")
	@Test
	public void testLongLine() {
		int length = 10000;
		try(
				ClearCLBuffer input = clij.push(img1D(new float[length + 2]));
				ClearCLBuffer kernel = clij.push(img1D(-0.5f, 0, 0.5f));
				ClearCLBuffer output = clij.create(new long[]{length, 1});
		) {
			Gauss.convolve(clij, input, kernel, output);
			ImgLib2Assert.assertImageEqualsRealType(img1D(new float[length]), clij.pullRAI(output), 0);
		}
	}

	@Test
	public void testY() {
		try(
				ClearCLBuffer input = clij.push(ArrayImgs.floats(new float[] {
							0, 0, 0, 0,
						    0, 1, 0, 0,
						    0, 0, -1, 0
						}, 4, 3));
				ClearCLBuffer kernel = clij.push(img1D(1, 2));
				ClearCLBuffer output = clij.create(new long[]{3, 2});
		) {
			Gauss.convolve(clij, CLIJView.interval(input, FinalInterval.createMinSize(1,0,2,2)), kernel, CLIJView.wrap(output), 1);
			RandomAccessibleInterval actual = clij.pullRAI(output);
			ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.floats(new float[]{
					2, 0, 0,
					1, -2, 0
			}, 3, 2), actual, 0);
		}
	}

	private ArrayImg<FloatType, FloatArray> img1D(float... array) {
		return ArrayImgs.floats(array, array.length, 1);
	}
}
