package net.imglib2.algorithm.features.classification;

import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

import java.util.List;

/**
 * @author Matthias Arzt
 */
public interface Training {

	void add(Composite<? extends RealType<?>> featureVector, int classIndex);

	void train();
}
