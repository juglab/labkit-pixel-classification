package net.imglib2.algorithm.features;

import com.google.gson.*;
import hr.irb.fastRandomForest.FastRandomForest;
import ij.Prefs;
import net.imglib2.*;
import net.imglib2.RandomAccess;
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
import java.lang.reflect.Type;
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

	public static Classifier train(Img<FloatType> image, ImgLabeling<String, IntType> labeling, FeatureGroup features) {
		return train(image, labeling, features, initRandomForest());
	}

	public static Classifier train(Img<FloatType> image, ImgLabeling<String, IntType> labeling, FeatureGroup features, weka.classifiers.Classifier classifier) {
		List<String> classNames = getClassNames(labeling);
		Training<Classifier> training = training(classNames, features, classifier);

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

	public List<String> classNames() {
		return classNames;
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

	private static List<String> getClassNames(ImgLabeling<String, IntType> labeling) {
		return new ArrayList<>(labeling.getMapping().getLabels());
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
