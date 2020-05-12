
package net.imglib2.trainable_segmentation.pixel_feature.filter.dog2;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmentation.pixel_feature.filter.stats.SingleStatisticsFeature;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSetting;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "difference of gaussians (group)")
public class DifferenceOfGaussiansFeature extends AbstractGroupFeatureOp {

	private List<Pair<Double, Double>> sigmaPairs(List<Double> sigmas) {
		List<Pair<Double, Double>> sigmaPairs = new ArrayList<>();
		for (double sigma1 : sigmas)
			for (double sigma2 : sigmas)
				if (sigma1 < sigma2)
					sigmaPairs.add(new ValuePair<>(sigma1, sigma2));
		return sigmaPairs;
	}

	@Override
	protected List<FeatureSetting> initFeatures() {
		List<Double> sigmas = globalSettings().sigmas();
		List<Pair<Double, Double>> pairs = sigmaPairs(sigmas);
		return pairs.stream()
			.map(sigma1And2 -> SingleFeatures.differenceOfGaussians(sigma1And2.getA(), sigma1And2.getB()))
			.collect(Collectors.toList());
	}

}
