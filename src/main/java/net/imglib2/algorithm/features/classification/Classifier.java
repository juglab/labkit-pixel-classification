package net.imglib2.algorithm.features.classification;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.Prefs;
import net.imglib2.*;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.RevampUtils;
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
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;

import static net.imglib2.algorithm.features.Features.applyOnImg;

/**
 * @author Matthias Arzt
 */
public class Classifier {

	private final FeatureGroup features;

	private List<String> classNames;

	private weka.classifiers.Classifier classifier;

	public Classifier(List<String> classNames, FeatureGroup features, weka.classifiers.Classifier classifier) {
		this.classNames = Collections.unmodifiableList(classNames);
		this.features = features;
		this.classifier = classifier;
	}

	public FeatureGroup features() {
		return features;
	}

	public RandomAccessibleInterval<IntType> apply(RandomAccessibleInterval<FloatType> image) {
		RandomAccessibleInterval<FloatType> featureValues = applyOnImg(features, image);
		return applyOnFeatures(featureValues);
	}

	public RandomAccessibleInterval<IntType> applyOnFeatures(RandomAccessibleInterval<FloatType> featureValues) {
		RandomAccessibleInterval<Instance> instances = instances(features, classNames, featureValues);
		return Predict.classify(instances, classifier);
	}

	public RandomAccessibleInterval<IntType> applyOnComposite(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> featureValues) {
		RandomAccessibleInterval<Instance> instances = instancesForComposite(features, classNames, featureValues);
		return Predict.classify(instances, classifier);
	}

	public List<String> classNames() {
		return classNames;
	}

	private static RandomAccessibleInterval<Instance> instances(Feature feature, List<String> classes, RandomAccessibleInterval<FloatType> featureValues) {
		return instancesForComposite(feature, classes, Views.collapseReal(featureValues));
	}

	private static <C extends Composite<? extends RealType<?>>> RandomAccessibleInterval<Instance> instancesForComposite(Feature feature, List<String> classes, RandomAccessibleInterval<C> collapsed) {
		return Views.interval(new InstanceView<>(collapsed, attributesAsArray(feature, classes)), collapsed);
	}

	private static Attribute[] attributesAsArray(Feature feature, List<String> classes) {
		List<Attribute> attributes = Features.attributes(feature, classes);
		return attributes.toArray(new Attribute[attributes.size()]);
	}

	private static Map<String, Feature> map = new HashMap();

	public void store(String filename) throws IOException {
		ClassifierSerialization.store(this, filename);
	}

	public static Classifier load(String filename) throws IOException {
		return ClassifierSerialization.load(filename);
	}

	public static Training<Classifier> training(List<String> classNames, FeatureGroup features, weka.classifiers.Classifier classifier) {
		return new MyTrainingData(classNames, features, classifier);
	}

	private static class MyTrainingData implements Training<Classifier> {

		final Instances instances;

		final int featureCount;

		final private List<String> classNames;

		final private FeatureGroup features;

		final private weka.classifiers.Classifier classifier;

		MyTrainingData(List<String> classNames, FeatureGroup features, weka.classifiers.Classifier classifier) {
			this.classNames = classNames;
			this.features = features;
			this.classifier = classifier;
			this.instances = new Instances("segment", new ArrayList<>(Features.attributes(features, classNames)), 1);
			this.featureCount = features.count();
			instances.setClassIndex(featureCount);
		}

		@Override
		public void add(Composite<? extends RealType<?>> featureVector, int classIndex) {
			instances.add(RevampUtils.getInstance(featureCount, classIndex, featureVector));
		}

		@Override
		public Classifier train() {
			RevampUtils.wrapException( () ->
 				classifier.buildClassifier(instances)
			);
			return new Classifier(classNames, features, classifier);
		}
	}
}
