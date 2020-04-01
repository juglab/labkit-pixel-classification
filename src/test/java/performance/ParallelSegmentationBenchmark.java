
package performance;

import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.gson.GsonUtils;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.StopWatch;
import net.imglib2.view.Views;
import org.scijava.Context;
import preview.net.imglib2.parallel.TaskExecutor;
import preview.net.imglib2.parallel.TaskExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ParallelSegmentationBenchmark {

	private static final Context context = new Context();

	private static final RandomAccessibleInterval<FloatType> image =
		Utils.loadImageFloatType("/home/arzt/Documents/Datasets/img_TL199_Chgreen.tif");

	private static final Segmenter segmenter =
		Segmenter.fromJson(context, GsonUtils.read(
			"/home/arzt/Documents/Datasets/img_TL199_Chgreen.classifier"));

	public static void main(String... args) {
		CLIJ2.getInstance().setKeepReferences(false);
		ImageJFunctions.show(image);
		segmenter.setUseGpu(true);
		CellLoader<UnsignedShortType> loader = cell -> segmenter.segment(cell, Views.extendBorder(
			image));
		int cellSize = 64;
		long imageSize = cellSize * 4;
		int[] cellDims = { cellSize, cellSize, cellSize };
		long[] imageDims = { imageSize, imageSize, imageSize };
		Img<UnsignedShortType> segmentation = createCellImage(loader, imageDims, cellDims);
		List<Interval> cells = getCells(new CellGrid(imageDims, cellDims));
		TaskExecutor executor = TaskExecutors.fixedThreadPool(4);
		StopWatch totalTime = StopWatch.createAndStart();
		executor.forEach(cells, cell -> {
			StopWatch watch = StopWatch.createAndStart();
			RandomAccess<UnsignedShortType> ra = segmentation.randomAccess();
			ra.setPosition(Intervals.minAsLongArray(cell));
			ra.get();
			System.out.println(watch);
		});
		totalTime.stop();
		System.out.println("Total time: " + totalTime);
		long timePerVoxel = totalTime.nanoTime() / Intervals.numElements(imageDims);
		System.out.println("Total time per voxel " + timePerVoxel + " ns");
		System.out.println("Total time per pixel " + timePerVoxel * cellSize / 1000 + " us");
	}

	private static List<Interval> getCells(CellGrid grid) {
		List<Interval> cells = new ArrayList<>();
		long numCells = Intervals.numElements(grid.getGridDimensions());
		for (int i = 0; i < numCells; i++) {
			long[] cellMin = new long[3];
			int[] cellDims = new int[3];
			grid.getCellDimensions(i, cellMin, cellDims);
			cells.add(FinalInterval.createMinSize(cellMin, IntStream.of(cellDims).mapToLong(x -> x)
				.toArray()));
		}
		return cells;
	}

	private static Img<UnsignedShortType> createCellImage(CellLoader<UnsignedShortType> loader,
		long[] imageSize, int[] cellSize)
	{
		ReadOnlyCachedCellImgFactory factory = new ReadOnlyCachedCellImgFactory(
			new ReadOnlyCachedCellImgOptions().cellDimensions(cellSize));
		return factory.create(imageSize, new UnsignedShortType(), loader);
	}
}
