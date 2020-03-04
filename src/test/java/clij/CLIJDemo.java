
package clij;

import com.google.gson.JsonElement;
import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.util.ElapsedTime;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.classification.CompositeInstance;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJCopy;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJMultiChannelImage;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJRandomForestKernel;
import net.imglib2.trainable_segmention.clij_random_forest.CLIJView;
import net.imglib2.trainable_segmention.clij_random_forest.RandomForestPrediction;
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

import java.util.List;

public class CLIJDemo {

	private static final int numberOfFeatures = 10;
	private static final int numberOfClasses = 2;

	public static void main(String... args) {

		CLIJ2 clij = CLIJ2.getInstance();
		OpService ops = new Context().service(OpService.class);
		JsonElement read = GsonUtils.read(CLIJDemo.class.getResource("/clij/test.classifier").getFile());
		Segmenter segmenter = Segmenter.fromJson(ops, read);
		Classifier classifier = segmenter.getClassifier();
		ElapsedTime.sStandardOutput = true;
		try {
			ImagePlus input = new ImagePlus("/home/arzt/Documents/Datasets/Example/small-3d-stack.tif");
			input.show();
			StopWatch stopWatch = StopWatch.createAndStart();
			RandomAccessibleInterval<? extends RealType<?>> image;
			try (
					ClearCLBuffer inputCl = clij.push(input);
					CLIJMultiChannelImage featuresCl = calculateFeatures(clij, inputCl);
					CLIJMultiChannelImage distributionCl = calculateDistribution(clij, classifier, featuresCl);
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

	private static ClearCLBuffer calculateSegmentation(CLIJ2 clij, CLIJMultiChannelImage distribution) {
		ClearCLBuffer result = clij.create(distribution.getSpatialDimensions(), NativeTypeEnum.Float);
		CLIJRandomForestKernel.findMax(clij, distribution, result);
		return result;
	}

	private static CLIJMultiChannelImage calculateDistribution(CLIJ2 clij, Classifier classifier,
		CLIJMultiChannelImage featuresCl)
	{
		RandomForestPrediction prediction = new RandomForestPrediction(Cast
			.unchecked(classifier), numberOfClasses, numberOfFeatures);
		CLIJMultiChannelImage output = new CLIJMultiChannelImage(clij, featuresCl.getSpatialDimensions(), numberOfClasses);
		prediction.distribution(clij, featuresCl, output);
		return output;
	}

	private static Img<UnsignedByteType> segment(Classifier classifier, Attribute[] attributes,
		ImagePlus output)
	{
		RandomForestPrediction prediction = new RandomForestPrediction(Cast.unchecked(classifier),
			numberOfClasses, numberOfFeatures);
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

	private static CLIJMultiChannelImage calculateFeatures(CLIJ2 clij, ClearCLBuffer inputCl) {
		try (ClearCLBuffer tmpCl = clij.create(inputCl)) {
			CLIJMultiChannelImage output = new CLIJMultiChannelImage(clij, inputCl.getDimensions(), numberOfFeatures);
			List<CLIJView> slices = output.channels();
			for (int i = 0; i < numberOfFeatures; i++) {
				float sigma = i * 2;
				clij.gaussianBlur(inputCl, tmpCl, sigma, sigma, sigma);
				CLIJCopy.copy(clij, CLIJView.wrap(tmpCl), slices.get(i));
			}
			return output;
		}
	}
}
