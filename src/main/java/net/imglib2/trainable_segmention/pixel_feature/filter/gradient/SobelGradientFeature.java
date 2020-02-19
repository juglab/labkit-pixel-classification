
package net.imglib2.trainable_segmention.pixel_feature.filter.gradient;

import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractSigmaGroupFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Deprecated
@Plugin(type = FeatureOp.class, label = "Sobel Gradient (Group)")
public class SobelGradientFeature extends AbstractSigmaGroupFeatureOp {

	public SobelGradientFeature() {
		super(true);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleSobelGradientFeature.class;
	}

	@Override
	public boolean checkGlobalSettings(GlobalSettings globals) {
		return globals.numDimensions() == 2;
	}
}
