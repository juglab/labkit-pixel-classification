
package net.imglib2.trainable_segmentation.pixel_feature.filter.laplacian;

import net.imglib2.trainable_segmentation.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmentation.pixel_feature.filter.FeatureOp;
import org.scijava.plugin.Plugin;

@Plugin(type = FeatureOp.class, label = "laplacian of gaussian (group)")
public class LaplacianOfGaussianFeature extends AbstractSigmaGroupFeatureOp {

	public LaplacianOfGaussianFeature() {
		super(false);
	}

	@Override
	public Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleLaplacianOfGaussianFeature.class;
	}
}
