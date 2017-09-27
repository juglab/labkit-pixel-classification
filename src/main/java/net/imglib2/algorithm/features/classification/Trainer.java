package net.imglib2.algorithm.features.classification;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.Prefs;
import net.imagej.ops.OpEnvironment;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.RealType;
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
 * {@linke Trainer} simplifies training a {@link Classifier}
 *
 * @author Matthias Arzt
 */
public class Trainer {

	private final FeatureGroup features;

	private final List<String> classNames;

	private final Training training;

	private Trainer(Classifier classifier) {
		features = classifier.features();
		training = classifier.training();
		classNames = classifier.classNames();
	}

	public static Trainer of(Classifier classifier) {
		return new Trainer(classifier);
	}

	public <T> void trainLabeledImage(Img<T> image, LabelRegions<String> labeling) {
		RandomAccessible<? extends GenericComposite<FloatType>> featureStack = Views.collapse(applyOnImg(features, image));
		trainLabeledFeatures(featureStack, labeling);
	}

	public void trainLabeledFeatures(RandomAccessible<? extends Composite<? extends RealType<?>>> features, LabelRegions<String> regions) {
		RandomAccess<? extends Composite<? extends RealType<?>>> ra = features.randomAccess();
		for(int classIndex = 0; classIndex < classNames.size(); classIndex++) {
			LabelRegion<String> region = regions.getLabelRegion(classNames.get(classIndex));
			Cursor<Void> cursor = region.cursor();
			while(cursor.hasNext()) {
				cursor.next();
				ra.setPosition(cursor);
				training.add(ra.get(), classIndex);
			}
		}
		training.train();
	}

	public static <T> Classifier train(OpEnvironment ops, Img<T> image, LabelRegions<String> labeling, FeatureGroup features) {
		return train(ops, image, labeling, features, initRandomForest());
	}

	public static <T> Classifier train(OpEnvironment ops, Img<T> image, LabelRegions<String> labeling, FeatureGroup features, weka.classifiers.Classifier initialWekaClassifier) {
		List<String> classNames = new ArrayList<>(labeling.getExistingLabels());
		Classifier classifier = new Classifier(ops, classNames, features, initialWekaClassifier);
		Trainer.of(classifier).trainLabeledImage(image, labeling);
		return classifier;
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
}
