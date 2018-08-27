package net.imglib2.trainable_segmention.pixel_feature.filter.gauss;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Gauss (Group)")
public class GaussFeature extends AbstractSigmaGroupFeatureOp {

	public GaussFeature() {
		super(false);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGaussFeature.class;
	}
}
