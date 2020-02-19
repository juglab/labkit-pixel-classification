
package net.imglib2.trainable_segmention.classification;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

/**
 * @author Matthias Arzt
 */
public interface Training {

	void add(Composite<? extends RealType<?>> featureVector, int classIndex);

	void train();
}
