package net.imglib2.algorithm.features.ops;

import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class GaussFeature extends AbstractSigmaGroupFeatureOp {

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return GaussFeatureOp.class;
	}
}
