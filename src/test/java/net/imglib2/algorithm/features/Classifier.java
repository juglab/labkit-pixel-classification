package net.imglib2.algorithm.features;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.Prefs;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;
import net.imglib2.view.composite.RealComposite;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.imglib2.algorithm.features.Features.applyOnImg;

/**
 * @author Matthias Arzt
 */
public class Classifier {

	private final Feature feature;

	private List<String> classNames;

	private weka.classifiers.Classifier classifier;

	public Classifier(List<String> classNames, Feature feature, weka.classifiers.Classifier classifier) {
		this.classNames = classNames;
		this.feature = feature;
		this.classifier = classifier;
	}

	public RandomAccessibleInterval<IntType> apply(RandomAccessibleInterval<FloatType> image) {
		RandomAccessibleInterval<Instance> instances = instances(feature, image, classNames);
		return Predict.classify(instances, classifier);
	}

	public static Classifier train(Img<FloatType> image, ImgLabeling<String, IntType> labeling, Feature feature) {
		weka.classifiers.Classifier classifier = initRandomForest();
		try {
			classifier.buildClassifier(trainingInstances(image, labeling, feature));
		} catch (Exception e) {
			new RuntimeException(e);
		}
		return new Classifier(getClassNames(labeling), feature, classifier);
	}

	private static AbstractClassifier initRandomForest() {
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

	private static Instances trainingInstances(Img<FloatType> image, ImgLabeling<String, IntType> labeling, Feature feature) {
		List<String> classNames = getClassNames(labeling);
		final List<Attribute> attributes = Features.attributes(feature, classNames);
		final Instances instances = new Instances("segment", new ArrayList<>(attributes), 1);
		int featureCount = feature.count();
		instances.setClassIndex(featureCount);

		RandomAccessible<? extends GenericComposite<FloatType>> featureStack = Views.collapse(applyOnImg(feature, image));
		RandomAccess<? extends GenericComposite<FloatType>> ra = featureStack.randomAccess();
		for(int classIndex = 0; classIndex < classNames.size(); classIndex++) {
			final LabelRegions<String> regions = new LabelRegions<>(labeling);
			LabelRegion<String> region = regions.getLabelRegion(classNames.get(classIndex));
			Cursor<Void> cursor = region.cursor();
			while(cursor.hasNext()) {
				cursor.next();
				ra.setPosition(cursor);
				instances.add(getInstance(featureCount, classIndex, ra.get()));
			}
		}
		return instances;
	}

	private static List<String> getClassNames(ImgLabeling<String, IntType> labeling) {
		return new ArrayList<>(labeling.getMapping().getLabels());
	}

	private static DenseInstance getInstance(int featureCount, int classIndex, GenericComposite<FloatType> featureValues) {
		double[] values = new double[featureCount + 1];
		for (int i = 0; i < featureCount; i++)
			values[i] = featureValues.get(i).get();
		values[featureCount] = classIndex;
		return new DenseInstance(1.0, values);
	}

	public static RandomAccessibleInterval<Instance> instances(Feature feature, RandomAccessibleInterval<FloatType> image, List<String> classes) {
		RandomAccessibleInterval<RealComposite<FloatType>> collapsed = Views.collapseReal(applyOnImg(feature, image));
		RandomAccessible<Instance> instanceView = new InstanceView<>(collapsed, attributesAsArray(feature, classes));
		return Views.interval(instanceView, image);
	}

	public static Attribute[] attributesAsArray(Feature feature, List<String> classes) {
		List<Attribute> attributes = Features.attributes(feature, classes);
		return attributes.toArray(new Attribute[attributes.size()]);
	}

}
