/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.classification;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.Prefs;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.utils.views.FastViews;
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
		RandomAccessible<Composite<FloatType>> featureStack = FastViews
			.collapse(features.apply(image));
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
