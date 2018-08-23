package net.imglib2.trainable_segmention.pixel_feature.filter.laplacian;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import org.scijava.plugin.Plugin;

@Plugin(type = FeatureOp.class, label = "Laplacian (Group)")
public class LaplacianFeature extends AbstractSigmaGroupFeatureOp {

	public LaplacianFeature() {
		super(false);
	}

	@Override
	public Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleLaplacianFeature.class;
	}
}
