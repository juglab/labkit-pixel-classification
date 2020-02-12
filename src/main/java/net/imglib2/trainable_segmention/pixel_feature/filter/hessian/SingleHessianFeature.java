
package net.imglib2.trainable_segmention.pixel_feature.filter.hessian;

import net.imglib2.*;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.img.Img;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Hessian")
public class SingleHessianFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
		return 8;
	}

	@Override
	public void apply(FeatureInput in, List<RandomAccessibleInterval<FloatType>> out) {
		RandomAccessibleInterval<FloatType> features = Views.stack(out);
		calculateHessianOnChannel(in, features, sigma);
	}

	List<String> LABELS = Arrays.asList("", "_Trace", "_Determinant", "_Eigenvalue_1",
		"_Eigenvalue_2",
		"_Orientation", "_Square_Eigenvalue_Difference", "_Normalized_Eigenvalue_Difference");

	@Override
	public List<String> attributeLabels() {
		return LABELS.stream().map(x -> "Hessian" + x + "_" + sigma / 0.4).collect(Collectors.toList());
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}

	private static final int HESSIAN = 0;
	private static final int TRACE = 1;
	private static final int DETERMINANT = 2;
	private static final int EIGENVALUE_1 = 3;
	private static final int EIGENVALUE_2 = 4;
	private static final int ORIENTATION = 5;
	private static final int SQUARE_EIGENVALUE_DIFFERENCE = 6;
	private static final int NORMALIZED_EIGENVALUE_DIFFERENCE = 7;

	private void calculateHessianOnChannel(FeatureInput image,
		RandomAccessibleInterval<FloatType> out, double sigma)
	{
		double[] sigmas = { sigma, sigma };

		RandomAccessibleInterval<DoubleType> dxx = image.derivedGauss(sigma, 2, 0);
		RandomAccessibleInterval<DoubleType> dxy = image.derivedGauss(sigma, 1, 1);
		RandomAccessibleInterval<DoubleType> dyy = image.derivedGauss(sigma, 0, 2);

		LoopBuilder.setImages(Views.collapseReal(out), dxx, dxy, dyy).forEachPixel((o, s_xx, s_xy,
			s_yy) -> {
			calculateHessianPerPixel(o, s_xx.get(), s_xy.get(), s_yy.get());
		});
	}

	private static void calculateHessianPerPixel(RealComposite<FloatType> output, double s_xx,
		double s_xy, double s_yy)
	{
		final double t = Math.pow(1, 0.75);

		// Hessian module: sqrt (a^2 + b*c + d^2)
		output.get(HESSIAN).setReal(Math.sqrt(s_xx * s_xx + s_xy * s_xy + s_yy * s_yy));
		// Trace: a + d
		final double trace = s_xx + s_yy;
		output.get(TRACE).setReal(trace);
		// Determinant: a*d - c*b
		final double determinant = s_xx * s_yy - s_xy * s_xy;
		output.get(DETERMINANT).setReal(determinant);

		// Ratio
		// ipRatio.setReal((trace*trace) / determinant);
		// First eigenvalue: (a + d) / 2 + sqrt( ( 4*b^2 + (a - d)^2) / 2 )
		output.get(EIGENVALUE_1).setReal(trace / 2.0 + Math.sqrt((4 * s_xy * s_xy + (s_xx - s_yy) *
			(s_xx - s_yy)) / 2.0));
		// Second eigenvalue: (a + d) / 2 - sqrt( ( 4*b^2 + (a - d)^2) / 2 )
		output.get(EIGENVALUE_2).setReal(trace / 2.0 - Math.sqrt((4 * s_xy * s_xy + (s_xx - s_yy) *
			(s_xx - s_yy)) / 2.0));
		// Orientation
		double orientation = ((s_xy < 0.0) ? -0.5 : 0.5) * Math.acos((s_xx - s_yy) / Math.sqrt(4.0 *
			s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)));
		if (Double.isNaN(orientation))
			orientation = 0;
		output.get(ORIENTATION).setReal(orientation);
		// Gamma-normalized square eigenvalue difference
		output.get(SQUARE_EIGENVALUE_DIFFERENCE).setReal(Math.pow(t, 4) * trace * trace * ((s_xx -
			s_yy) * (s_xx - s_yy) + 4 * s_xy * s_xy));
		// Square of Gamma-normalized eigenvalue difference
		output.get(NORMALIZED_EIGENVALUE_DIFFERENCE).setReal(Math.pow(t, 2) * ((s_xx - s_yy) * (s_xx -
			s_yy) + 4 * s_xy * s_xy));
	}
}
