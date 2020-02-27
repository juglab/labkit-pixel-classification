
package clij;

import com.google.gson.JsonElement;
import hr.irb.fastRandomForest.FastRandomForest;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.util.ElapsedTime;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.classification.CompositeInstance;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.gson.GsonUtils;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.StopWatch;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;
import org.scijava.Context;
import preview.net.imglib2.loops.LoopBuilder;
import weka.classifiers.Classifier;
import weka.core.Attribute;

import java.util.HashMap;
import java.util.Map;

public class ClijDemo {

	private static final int numberOfFeatures = 10;
	private static final int numberOfClasses = 2;

	public static void main(String... args) {

		CLIJ clij = CLIJ.getInstance();
		OpService ops = new Context().service(OpService.class);
		JsonElement read = GsonUtils.read(ClijDemo.class.getResource("/clij/test.classifier").getFile());
		Segmenter segmenter = Segmenter.fromJson(ops, read);
		Classifier classifier = segmenter.getClassifier();
		Attribute[] attributes = segmenter.attributesAsArray();
		ElapsedTime.sStandardOutput = true;
		try {
			ImagePlus input = new ImagePlus("/home/arzt/Documents/Datasets/Example/small-3d-stack.tif");
			input.show();
			RandomAccessibleInterval<? extends RealType<?>> image;
			StopWatch stopWatch = StopWatch.createAndStart();
			try (
				ClearCLBuffer inputCl = clij.push(input);
				ClearCLBuffer featuresCl = calculateFeatures(clij, inputCl);
				ClearCLBuffer distributionCl = calculateDistribution(clij, classifier, featuresCl);
				ClearCLBuffer segmentationCl = calculateSegmentation(clij, distributionCl))
			{
				image = clij.pullRAI(segmentationCl);
			}
			System.out.println(stopWatch);
			ImageJFunctions.show(Cast.unchecked(image)).setDisplayRange(0, 1);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			clij.close();
		}
	}

	private static ClearCLBuffer calculateSegmentation(CLIJ clij, ClearCLBuffer distribution) {
		long slices = distribution.getDepth() / numberOfClasses;
		ClearCLBuffer result = clij.createCLBuffer(new long[] { distribution.getWidth(), distribution
			.getHeight(), slices }, NativeTypeEnum.Float);
		ClijRandomForestKernel.findMax(clij, distribution, result, numberOfClasses);
		return result;
	}

	private static ClearCLBuffer calculateDistribution(CLIJ clij, Classifier classifier,
		ClearCLBuffer featuresCl)
	{
		RandomForestPrediction prediction = new RandomForestPrediction(new MyRandomForest(Cast
			.unchecked(classifier)), 2);
		long slices = featuresCl.getDepth() / numberOfFeatures;
		ClearCLBuffer distribution = clij.createCLBuffer(new long[] { featuresCl.getWidth(), featuresCl
			.getHeight(), slices * numberOfClasses }, NativeTypeEnum.Float);
		prediction.distribution(clij, featuresCl, distribution);
		return distribution;
	}

	private static Img<UnsignedByteType> segment(Classifier classifier, Attribute[] attributes,
		ImagePlus output)
	{
		MyRandomForest forest = new MyRandomForest((FastRandomForest) classifier);
		RandomForestPrediction prediction = new RandomForestPrediction(forest, 2);
		RandomAccessibleInterval<FloatType> featureStack =
			Views.permute(ImageJFunctions.wrapFloat(output), 2, 3);
		CompositeIntervalView<FloatType, RealComposite<FloatType>> collapsed =
			Views.collapseReal(featureStack);
		CompositeInstance compositeInstance =
			new CompositeInstance(collapsed.randomAccess().get(), attributes);
		Img<UnsignedByteType> segmentation =
			ArrayImgs.unsignedBytes(Intervals.dimensionsAsLongArray(collapsed));
		StopWatch stopWatch = StopWatch.createAndStart();
		LoopBuilder.setImages(collapsed, segmentation).forEachPixel((c, o) -> {
			compositeInstance.setSource(c);
			o.set(prediction.classifyInstance(compositeInstance));
		});
		System.out.println(stopWatch);
		return segmentation;
	}

	private static ClearCLBuffer calculateFeatures(CLIJ clij, ClearCLBuffer inputCl) {
		try (ClearCLBuffer tmpCl = clij.createCLBuffer(inputCl)) {
			ClearCLBuffer outputCl = clij.createCLBuffer(new long[] { inputCl.getWidth(), inputCl
				.getHeight(), inputCl.getDepth() * numberOfFeatures }, NativeTypeEnum.Float);
			for (int i = 0; i < numberOfFeatures; i++) {
				float sigma = i * 2;
				clij.op().blur(inputCl, tmpCl, sigma, sigma, sigma);
				copy3dStack(clij, tmpCl, outputCl, i, numberOfFeatures);
			}
			return outputCl;
		}
	}

	private static void copy3dStack(CLIJ clij, ClearCLBuffer input, ClearCLBuffer output, int offset,
		int step)
	{
		long[] globalSizes = input.getDimensions();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("src", input);
		parameters.put("dst", output);
		parameters.put("offset", offset);
		parameters.put("step", step);
		clij.execute(ClijDemo.class, "copy_3d_stack.cl", "copy_3d_stack", globalSizes,
			parameters);
	}

	private static void copy(CLIJ clij, ClearCLBuffer inputCl, Interval sourceInterval,
		ClearCLBuffer outputCl, Interval destinationInterval)
	{
		long[] globalSizes = Intervals.dimensionsAsLongArray(sourceInterval);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("src", inputCl);
		parameters.put("dst", outputCl);
		parameters.put("src_offset_x", (int) sourceInterval.min(0));
		parameters.put("src_offset_y", (int) sourceInterval.min(1));
		parameters.put("src_offset_z", (int) sourceInterval.min(2));
		parameters.put("dst_offset_x", (int) destinationInterval.min(0));
		parameters.put("dst_offset_y", (int) destinationInterval.min(1));
		parameters.put("dst_offset_z", (int) destinationInterval.min(2));
		clij.execute(ClijDemo.class, "copy_with_offset.cl", "copy_with_offset", globalSizes,
			parameters);
	}

	private static Interval interval(ClearCLBuffer inputCl) {
		return new FinalInterval(inputCl.getDimensions());
	}

	private static void crop(CLIJ clij, ClearCLBuffer inputCl, ClearCLBuffer outputCl) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("src", inputCl);
		parameters.put("dst", outputCl);
		parameters.put("start_x", 0);
		parameters.put("start_y", 0);
		parameters.put("start_z", 0);
		clij.execute(Kernels.class, "duplication.cl", "crop_3d", parameters);
	}
}
