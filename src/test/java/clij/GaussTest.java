package clij;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.util.HashMap;

public class GaussTest {

	CLIJ2 clij = CLIJ2.getInstance();

	float[] kernel = {-0.5f, 0, 0.5f};

	@Test
	public void test() {
		Img<FloatType> input = ArrayImgs.floats(new float[]{0, 0, 1, 0, 0}, 5, 1);
		Img<FloatType> kernelImg = ArrayImgs.floats(kernel, kernel.length, 1);
		Img<FloatType> expected = ArrayImgs.floats(new float[]{0.5f, 0, -0.5f}, 3, 1);
		ClearCLBuffer inputBuffer = clij.push(input);
		ClearCLBuffer kernelBuffer = clij.push(kernelImg);
		ClearCLBuffer outputBuffer = clij.create(new long[]{3, 1});
		Gauss.convolve(clij, inputBuffer, kernelBuffer, outputBuffer);
		RandomAccessibleInterval output = clij.pullRAI(outputBuffer);
		ImgLib2Assert.assertImageEqualsRealType(expected, output, 0);
	}
}
