
package net.imglib2.trainable_segmention.classification;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.Prefs;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.GenericComposite;
import org.scijava.Context;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * {@link Trainer} simplifies training a {@link Segmenter}
 *
 * @author Matthias Arzt
 */
public class Trainer {

	private final FeatureCalculator features;

	private final List<String> classNames;

	private final Training training;

	private boolean autoFinish = true;

	private boolean finished = false;

	private Trainer(Segmenter segmenter) {
		features = segmenter.features();
		training = segmenter.training();
		classNames = segmenter.classNames();
	}

	public static Trainer of(Segmenter segmenter) {
		return new Trainer(segmenter);
	}

	public void start() {
		autoFinish = false;
	}

	public void finish() {
		if (finished)
			throw new IllegalStateException();
		finished = true;
		training.train();
	}

	public void trainLabeledImage(RandomAccessibleInterval<?> image, LabelRegions<?> labeling) {
		RandomAccessible<? extends GenericComposite<FloatType>> featureStack = Views.collapse(features
			.apply(image));
		trainLabeledFeatures(featureStack, labeling);
	}

	public <L> void trainLabeledFeatures(
		RandomAccessible<? extends Composite<? extends RealType<?>>> features, LabelRegions<L> regions)
	{
		RandomAccess<? extends Composite<? extends RealType<?>>> ra = features.randomAccess();
		Map<String, L> kayMap = createKeyMap(regions);
		for (int classIndex = 0; classIndex < classNames.size(); classIndex++) {
			L label = kayMap.get(classNames.get(classIndex));
			if (label == null)
				continue;
			LabelRegion<L> region = regions.getLabelRegion(label);
			Cursor<Void> cursor = region.cursor();
			while (cursor.hasNext()) {
				cursor.next();
				ra.setPosition(cursor);
				training.add(ra.get(), classIndex);
			}
		}
		if (autoFinish)
			finish();
	}

	private <L> Map<String, L> createKeyMap(LabelRegions<L> regions) {
		Map<String, L> map = new HashMap<>();
		regions.getExistingLabels().forEach(label -> map.put(label.toString(), label));
		return map;
	}

	public static Segmenter train(Context context, RandomAccessibleInterval<?> image,
		LabelRegions<?> labeling, FeatureSettings features)
	{
		return train(context, image, labeling, features, initRandomForest());
	}

	public static Segmenter train(Context context, RandomAccessibleInterval<?> image,
		LabelRegions<?> labeling, FeatureSettings features, Classifier initialWekaClassifier)
	{
		List<String> classNames = labeling.getExistingLabels().stream().map(Object::toString).collect(
			Collectors.toList());
		Segmenter segmenter = new Segmenter(context, classNames, features, initialWekaClassifier);
		Trainer.of(segmenter).trainLabeledImage(image, labeling);
		return segmenter;
	}

	public static AbstractClassifier initRandomForest() {
		FastRandomForest rf = new FastRandomForest();
		int numOfTrees = 200;
		rf.setNumTrees(numOfTrees);
		// this is the default that Breiman suggests
		// rf.setNumFeatures((int) Math.round(Math.sqrt(featureStack.getSize())));
		// but this seems to work better
		int randomFeatures = 2;
		rf.setNumFeatures(randomFeatures);
		// Random seed
		rf.setSeed(1);
		// Set number of threads
		rf.setNumThreads(Prefs.getThreads());
		return rf;
	}
}
