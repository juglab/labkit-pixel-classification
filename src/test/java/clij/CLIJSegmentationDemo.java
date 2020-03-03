package clij;

import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class CLIJSegmentationDemo {

	static {
		LegacyInjector.preinit();
	}

	public static void main(String... args) {
		CLIJ2 clij = CLIJ2.getInstance();
		try {
			ImagePlus inputImagePlus = new ImagePlus("/home/arzt/Documents/Datasets/Example/small-3d-stack.tif");
			ImgPlus<FloatType> input = VirtualStackAdapter.wrapFloat(inputImagePlus);
			FeatureCalculator calculator = FeatureCalculator.default2d()
				.addFeature(SingleFeatures.gauss(8))
				.build();
			FinalInterval interval = Intervals.expand(input, -40);
			try(ClearCLBuffer resultClBuffer = calculator.applyWithCLIJ(Views.extendBorder(input), interval))
			{
				RandomAccessibleInterval<FloatType> result = Views.addDimension(clij.pullRAI(resultClBuffer), 0, 0);
				RandomAccessibleInterval<FloatType> expected = Views.zeroMin(calculator.apply(Views.extendBorder(input), interval));
				Utils.showPsnr(expected, result);
			}
		}
		finally {
			clij.close();
		}
	}
}
