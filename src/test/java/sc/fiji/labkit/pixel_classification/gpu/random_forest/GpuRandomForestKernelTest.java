
package net.imglib2.trainable_segmentation.gpu.random_forest;

import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmentation.gpu.api.AbstractGpuTest;
import net.imglib2.trainable_segmentation.gpu.api.GpuImage;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

/**
 * Test {@link GpuRandomForestKernel}
 */
public class GpuRandomForestKernelTest extends AbstractGpuTest {

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
			0, Short.MIN_VALUE, 1,
			0, Short.MIN_VALUE + 1, Short.MIN_VALUE + 2,

			0, Short.MIN_VALUE, Short.MIN_VALUE + 1,
			0, 0, 0
		}, 3, numberOfNodes, numberOfTrees);

		GpuRandomForestKernel.randomForest(gpu,
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
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.00001);
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
		GpuRandomForestKernel.findMax(gpu, gpu.pushMultiChannel(input), outputBuffer);
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
		GpuRandomForestKernel.findMax(gpu, gpu.pushMultiChannel(input), outputBuffer);
		RandomAccessibleInterval<? extends RealType<?>> result = gpu.pullRAI(outputBuffer);
		RandomAccessibleInterval<FloatType> expected = ArrayImgs.floats(new float[] {
			1, 0,
			0, 1
		}, 2, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

}
