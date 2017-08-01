package net.imglib2.algorithm.features.classification;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.Prefs;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.GenericComposite;
import weka.classifiers.AbstractClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.imglib2.algorithm.features.Features.applyOnImg;

/**
 * @author Matthias Arzt
 */
public interface Training<T> {


	void add(Composite<? extends RealType<?>> featureVector, int classIndex);

	T train();
}
