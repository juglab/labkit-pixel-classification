
package net.imglib2.trainable_segmentation;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.trainable_segmentation.classification.Segmenter;
import net.imglib2.trainable_segmentation.classification.Trainer;
import net.imglib2.trainable_segmentation.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.scijava.Context;
import preview.net.imglib2.loops.LoopBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 15, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class SegmentationBenchmark {

	private final Context context = SingletonContext.getInstance();

	private final RandomAccessibleInterval<FloatType> image = Utils.loadImageFloatType(
			"drosophila_3d.tif");

	private final LabelRegions<?> labelRegions = asLabelRegions(Utils.loadImageFloatType(
			"drosophila_3d_labeling.tif"));

	@Param(value={"false", "true"})
	private boolean slowdown;

	@Setup
	public void setup() {
		if(slowdown) {
			Segmenter segmenter = trainSegmenter();
			segmenter.segment(image);
		}
	}

	private Segmenter trainSegmenter() {
		final FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default3d().build(),
			GroupedFeatures.gauss(),
			GroupedFeatures.differenceOfGaussians(),
			GroupedFeatures.hessian(),
			GroupedFeatures.gradient());
		return Trainer.train(context, image, labelRegions, featureSettings);
	}

	private final Img<? extends RealType<?>> floats = ArrayImgs.ints(1000, 1000);

	@Benchmark
	public Object sum() {
		double sum = 0;
		RandomAccess< ? extends RealType< ? > > ra = floats.randomAccess();
		ra.setPosition(0, 1);
		for (int y = 0; y < 1000; y++) {
			ra.setPosition(0, 0);
			for (int x = 0; x < 1000; x++) {
				sum += ra.get().getRealDouble();
				ra.fwd(0);
			}
			ra.fwd(1);
		}
		return sum;
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder()
				.include(SegmentationBenchmark.class.getSimpleName())
				.build();
		new Runner(options).run();
	}

	// -- Helper --

	private LabelRegions asLabelRegions(RandomAccessibleInterval<? extends RealType<?>> labeling) {
		Img<UnsignedByteType> ints = ArrayImgs.unsignedBytes(Intervals.dimensionsAsLongArray(labeling));
		RandomAccessibleInterval<LabelingType<String>> labelingTypes = new ImgLabeling<>(ints);
		LoopBuilder.setImages(labeling, labelingTypes).multiThreaded().forEachPixel((i, o) -> {
			if (i.getRealFloat() != 0) o.add(i.toString());
		});
		return new LabelRegions<>(labelingTypes);
	}

}
