
package net.imglib2.trainable_segmention.clij_random_forest;

import clij.GpuImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import clij.GpuApi;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmention.utils.ToString;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CLIJRandomForestKernelTest {

	private static GpuApi gpu;

	@Before
	public void before() {
		gpu = GpuApi.getInstance();
	}

	@After
	public void after() {
		gpu.close();
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
		GpuImage distributions = gpu.create(new long[] { width, height, depth }, numberOfClasses,
			NativeTypeEnum.Float);
		Img<FloatType> src = ArrayImgs.floats(new float[] {
			41, 43, 45,
			45, 43, 41
		}, width, height, depth, numberOfFeatures);
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
		Img<UnsignedShortType> indices = ArrayImgs.unsignedShorts(new short[] {
			0, 2, 1,
			0, 3, 4,

			0, 2, 3,
			0, 0, 0
		}, 3, numberOfNodes, numberOfTrees);

		CLIJRandomForestKernel.randomForest(gpu,
			distributions,
			gpu.pushMultiChannel(src),
			gpu.push(thresholds),
			gpu.push(probabilities),
			gpu.push(indices),
			numberOfFeatures);

		RandomAccessibleInterval<? extends RealType<?>> result = gpu.pullRAIMultiChannel(distributions);
		Img<FloatType> expected = ArrayImgs.floats(new float[] {
			0.4f, 0.75f, 0.5f,
			0.5f, 0.75f, 0.4f,

			0.6f, 0.25f, 0.5f,
			0.5f, 0.25f, 0.6f
		}, 3, 1, 2, 2);
		ToString.print(result);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.00001);
		Views.iterable(result).forEach(System.out::println);

	}

	@Test
	public void testFindMax() {
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			1, 3, 2, 2,
			-1, -2, -3, -3,

			2, 2, 3, 1,
			-2, -1, -1, -1,

			3, 1, 1, 3,
			-3, -3, -2, -1
		}, 2, 2, 2, 3);
		GpuImage outputBuffer = gpu.create(new long[] { 2, 2, 2 }, NativeTypeEnum.UnsignedShort);
		CLIJRandomForestKernel.findMax(gpu, gpu.pushMultiChannel(input), outputBuffer);
		RandomAccessibleInterval<? extends RealType<?>> result = gpu.pullRAI(outputBuffer);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] {
			2, 0, 1, 2,
			0, 1, 1, 1
		}, 2, 2, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test
	public void testFindMax2d() {
		Img<FloatType> input = ArrayImgs.floats(new float[] {
			1, 3,
			-1, -2,

			2, 2,
			-2, -1,
		}, 2, 2, 2);
		GpuImage outputBuffer = gpu.create(new long[] { 2, 2 }, NativeTypeEnum.UnsignedShort);
		CLIJRandomForestKernel.findMax(gpu, gpu.pushMultiChannel(input), outputBuffer);
		RandomAccessibleInterval<? extends RealType<?>> result = gpu.pullRAI(outputBuffer);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] {
			1, 0,
			0, 1
		}, 2, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

}
