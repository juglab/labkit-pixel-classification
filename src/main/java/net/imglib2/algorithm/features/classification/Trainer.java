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
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;
import weka.classifiers.AbstractClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.imglib2.algorithm.features.Features.applyOnImg;

/**
 * {@linke Trainer} simplifies training a {@link Classifier}
 *
 * @author Matthias Arzt
 */
public class Trainer {

	public static Classifier train(Img<FloatType> image, ImgLabeling<String, IntType> labeling, FeatureGroup features) {
		return train(image, labeling, features, initRandomForest());
	}

	public static Classifier train(Img<FloatType> image, ImgLabeling<String, IntType> labeling, FeatureGroup features, weka.classifiers.Classifier classifier) {
		List<String> classNames = getClassNames(labeling);
		Training<Classifier> training = Classifier.training(classNames, features, classifier);
		RandomAccessible<? extends GenericComposite<FloatType>> featureStack = Views.collapse(applyOnImg(features, image));
		RandomAccess<? extends GenericComposite<FloatType>> ra = featureStack.randomAccess();
		for(int classIndex = 0; classIndex < classNames.size(); classIndex++) {
			final LabelRegions<String> regions = new LabelRegions<>(labeling);
			LabelRegion<String> region = regions.getLabelRegion(classNames.get(classIndex));
			Cursor<Void> cursor = region.cursor();
			while(cursor.hasNext()) {
				cursor.next();
				ra.setPosition(cursor);
				training.add(ra.get(), classIndex);
			}
		}
		return training.train();
	}

	public static AbstractClassifier initRandomForest() {
		FastRandomForest rf = new FastRandomForest();
		int numOfTrees = 200;
		rf.setNumTrees(numOfTrees);
		//this is the default that Breiman suggests
		//rf.setNumFeatures((int) Math.round(Math.sqrt(featureStack.getSize())));
		//but this seems to work better
		int randomFeatures = 2;
		rf.setNumFeatures(randomFeatures);
		// Random seed
		rf.setSeed( (new Random()).nextInt() );
		// Set number of threads
		rf.setNumThreads( Prefs.getThreads() );
		return rf;
	}

	// private Helper methods

	private static List<String> getClassNames(ImgLabeling<String, IntType> labeling) {
		return new ArrayList<>(labeling.getMapping().getLabels());
	}
}
