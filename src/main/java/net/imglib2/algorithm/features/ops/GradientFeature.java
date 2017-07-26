package net.imglib2.algorithm.features.ops;

import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class)
public class GradientFeature extends AbstractSigmaGroupFeatureOp {

	public GradientFeature() {
		super(true);
	}

	@Override
	protected Class<? extends FeatureOp> getSingleFeatureClass() {
		return SingleGradientFeature.class;
	}
}
