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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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

	public <L> void trainLabeledImage(Img<?> image, LabelRegions<L> labeling) {
		RandomAccessible<? extends GenericComposite<FloatType>> featureStack = Views.collapse(applyOnImg(features, image));
		trainLabeledFeatures(featureStack, labeling);
	}

	public <L> void trainLabeledFeatures(RandomAccessible<? extends Composite<? extends RealType<?>>> features, LabelRegions<L> regions) {
		RandomAccess<? extends Composite<? extends RealType<?>>> ra = features.randomAccess();
		Map<String, L> kayMap = createKeyMap(regions);
		for(int classIndex = 0; classIndex < classNames.size(); classIndex++) {
			L label = kayMap.get(classNames.get(classIndex));
			if(label == null)
				continue;
			LabelRegion<L> region = regions.getLabelRegion(label);
			Cursor<Void> cursor = region.cursor();
			while(cursor.hasNext()) {
				cursor.next();
				ra.setPosition(cursor);
				training.add(ra.get(), classIndex);
			}
		}
		training.train();
	}

	private <L> Map<String,L> createKeyMap(LabelRegions<L> regions) {
		Map<String, L> map = new HashMap<>();
		regions.getExistingLabels().forEach(label -> map.put(label.toString(), label));
		return map;
	}

	public static Classifier train(OpEnvironment ops, Img<?> image, LabelRegions<?> labeling, FeatureGroup features) {
		return train(ops, image, labeling, features, initRandomForest());
	}

	public static Classifier train(OpEnvironment ops, Img<?> image, LabelRegions<?> labeling, FeatureGroup features, weka.classifiers.Classifier initialWekaClassifier) {
		List<String> classNames = labeling.getExistingLabels().stream().map(Object::toString).collect(Collectors.toList());
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
