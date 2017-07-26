package net.imglib2.algorithm.features.ops;

import org.scijava.plugin.Plugin;

import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class SobelGradientFeature extends AbstractSigmaGroupFeatureOp {

	public SobelGradientFeature() {
		super(true);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleSobelGradientFeature.class;
	}
}
