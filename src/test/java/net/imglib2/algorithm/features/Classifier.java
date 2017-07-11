package net.imglib2.algorithm.features;

import com.google.gson.*;
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

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import static net.imglib2.algorithm.features.Features.applyOnImg;

/**
 * @author Matthias Arzt
 */
public class Classifier {

	private final FeatureGroup feature;

	private List<String> classNames;

	private weka.classifiers.Classifier classifier;

	public Classifier(List<String> classNames, Feature feature, weka.classifiers.Classifier classifier) {
		this.classNames = classNames;
		this.feature = new FeatureGroup( feature );
		this.classifier = classifier;
	}

	public RandomAccessibleInterval<IntType> apply(RandomAccessibleInterval<FloatType> image) {
		RandomAccessibleInterval<Instance> instances = instances(feature, image, classNames);
		return Predict.classify(instances, classifier);
	}

	public static Classifier train(Img<FloatType> image, ImgLabeling<String, IntType> labeling, Feature feature) {
		return train(image, labeling, feature, initRandomForest());
	}

	public static Classifier train(Img<FloatType> image, ImgLabeling<String, IntType> labeling, Feature feature, weka.classifiers.Classifier classifier) {
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

	private static Map<String, Feature> map = new HashMap();

	public void store(String filename) throws IOException {
		try( FileWriter fw = new FileWriter(filename) ) {
			Gson gson = initGson();
			gson.toJson(this, Classifier.class, fw);
			fw.append("\n");
		}
	}

	public static Classifier load(String filename) throws IOException {
		try( FileReader fr = new FileReader(filename) ) {
			return initGson().fromJson(fr, Classifier.class);
		}
	}

	private static Gson initGson() {
		return Features.gsonModifiers(new GsonBuilder())
				.registerTypeAdapter(weka.classifiers.Classifier.class, new ClassifierSerializer())
				.registerTypeAdapter(weka.classifiers.Classifier.class, new ClassifierDeserializer())
				.create();
	}

	static class ClassifierSerializer implements JsonSerializer<weka.classifiers.Classifier> {

		@Override
		public JsonElement serialize(weka.classifiers.Classifier classifier, Type type, JsonSerializationContext json) {
			return new JsonPrimitive(Base64.getEncoder().encodeToString(objectToBytes(classifier)));
		}
	}

	static class ClassifierDeserializer implements JsonDeserializer<weka.classifiers.Classifier> {

		@Override
		public weka.classifiers.Classifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext json) throws JsonParseException {
			return (weka.classifiers.Classifier) bytesToObject(Base64.getDecoder().decode(jsonElement.getAsString()));
		}
	}

	private static byte[] objectToBytes(Object object) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object bytesToObject(byte[] bytes) {
		InputStream stream = new ByteArrayInputStream(bytes);
		try {
			return new ObjectInputStream(stream).readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
