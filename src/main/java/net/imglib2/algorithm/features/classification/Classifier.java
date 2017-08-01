package net.imglib2.algorithm.features.classification;

import net.imglib2.*;
import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.Composite;
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

	private final List<String> classNames;

	private weka.classifiers.Classifier classifier;

	private boolean isTrained = false;

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
		RandomAccessibleInterval<Instance> instances = InstanceView.wrap(features, classNames, featureValues);
		return Predict.classify(instances, classifier);
	}

	public RandomAccessibleInterval<IntType> applyOnComposite(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> featureValues) {
		RandomAccessibleInterval<Instance> instances = InstanceView.wrapComposite(features, classNames, featureValues);
		return Predict.classify(instances, classifier);
	}

	public List<String> classNames() {
		return classNames;
	}

	private static Map<String, Feature> map = new HashMap();

	public void store(String filename) throws IOException {
		ClassifierSerialization.store(this, filename);
	}

	public static Classifier load(String filename) throws IOException {
		return ClassifierSerialization.load(filename);
	}

	public Training training() {
		return new MyTrainingData();
	}

	public boolean isTrained() {
		return isTrained;
	}

	private class MyTrainingData implements Training {

		final Instances instances;

		final int featureCount;

		MyTrainingData() {
			this.instances = new Instances("segment", new ArrayList<>(Features.attributes(features, classNames)), 1);
			this.featureCount = features.count();
			instances.setClassIndex(featureCount);
		}

		@Override
		public void add(Composite<? extends RealType<?>> featureVector, int classIndex) {
			instances.add(RevampUtils.getInstance(featureCount, classIndex, featureVector));
		}

		@Override
		public void train() {
			RevampUtils.wrapException( () ->
 				classifier.buildClassifier(instances)
			);

		}
	}
}
