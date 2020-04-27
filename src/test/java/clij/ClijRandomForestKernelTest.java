
package clij;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClijRandomForestKernelTest {

	private static CLIJ clij;

	@Before
	public void before() {
		clij = CLIJ.getInstance();
	}

	@After
	public void after() {
		clij.close();
	}

	@Test
	public void testRandomForest() {
		int width = 3;
		int height = 1;
		int depth = 2;
		int numberOfTrees = 2;
		int numberOfFeatures = 1;
		int numberOfClasses = 2;
		int numberOfNodes = 2;
		int numberOfLeafs = 3;
		ClearCLBuffer distributions = clij.create(
			new long[] { width, height, numberOfClasses * depth }, NativeTypeEnum.Float);
		Img<FloatType> src = ArrayImgs.floats(new float[] {
			41, 43, 45,
			45, 43, 41
		}, width, height, numberOfFeatures * depth);
		Img<FloatType> thresholds = ArrayImgs.floats(new float[] {
			42, 44,
			43.5f, 0,
		}, 1, numberOfNodes, numberOfTrees);
		Img<FloatType> probabilities = ArrayImgs.floats(new float[] {
			2, 2,
			3, 0,
			4, 4,

			0, 1,
			0, 0,
			0, 0
		}, numberOfClasses, numberOfLeafs, numberOfTrees);
		Img<FloatType> indices = ArrayImgs.floats(new float[] {
			0, -1, 1,
			0, -2, -3,

			0, -1, -2,
			0, 0, 0
		}, 3, numberOfNodes, numberOfTrees);

		ClijRandomForestKernel.randomForest(clij,
			distributions,
			clij.push(src),
			clij.push(thresholds),
			clij.push(probabilities),
			clij.push(indices),
			numberOfTrees,
			numberOfClasses,
			numberOfFeatures);

		RandomAccessibleInterval<? extends RealType<?>> result = clij.pullRAI(distributions);
		Img<FloatType> expected = ArrayImgs.floats(new float[] {
			0.4f, 0.75f, 0.5f,
			0.6f, 0.25f, 0.5f,

			0.5f, 0.75f, 0.4f,
			0.5f, 0.25f, 0.6f
		}, 3, 1, 4);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.00001);
		Views.iterable(result).forEach(System.out::println);

	}

	@Test
	public void testFindMax() {
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			1, 3, 2, 2,
			2, 2, 3, 1,
			3, 1, 1, 3,

			-1, -2, -3, -3,
			-2, -1, -1, -1,
			-3, -3, -2, -1
		}, 2, 2, 6);
		ClearCLBuffer outputBuffer = clij.createCLBuffer(new long[] { 2, 2, 2 }, NativeTypeEnum.Float);
		ClijRandomForestKernel.findMax(clij, clij.push(input), outputBuffer, 3);
		RandomAccessibleInterval<? extends RealType<?>> result = clij.pullRAI(outputBuffer);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] {
			2, 0, 1, 2,
			0, 1, 1, 1
		}, 2, 2, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

}
