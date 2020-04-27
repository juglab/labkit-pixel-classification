
package net.imglib2.trainable_segmention.performance;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.gson.GsonUtils;
import net.imglib2.trainable_segmention.utils.SingletonContext;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.StopWatch;
import net.imglib2.view.Views;
import org.scijava.Context;

public class ParallelSegmentationTask implements Runnable {

	private static final Context context = SingletonContext.getInstance();

	private static final RandomAccessibleInterval<FloatType> image =
		Utils.loadImageFloatType("https://imagej.net/images/t1-head.zip");

	private final Segmenter segmenter = Segmenter.fromJson(context,
		GsonUtils.read(ParallelSegmentationTask.class.getResourceAsStream("/clij/t1-head.classifier")));

	private int cellSize = 64;
	private final int[] cellDims = { cellSize, cellSize, cellSize };
	private final long[] imageDims = { 256, 256, 128 };

	private StopWatch measuredTime;

	private Img<UnsignedShortType> segmenation;

	public void setUseGpu(boolean useGpu) {
		segmenter.setUseGpu(useGpu);
	}

	@Override
	public void run() {
		CellLoader<UnsignedShortType> loader = cell -> {
			segmenter.segment(cell, Views.extendBorder(image));
		};
		CachedCellImg<UnsignedShortType, ?> segmentation = createCellImage(loader, imageDims, cellDims);
		StopWatch totalTime = StopWatch.createAndStart();
		Utils.populateCellImg(segmentation);
		totalTime.stop();
		this.measuredTime = totalTime;
		this.segmenation = segmentation;
	}

	public Img<UnsignedShortType> getSegmenation() {
		return segmenation;
	}

	public StopWatch measuredTime() {
		return measuredTime;
	}

	public void printTimes() {
		System.out.println("Total time: " + measuredTime);
		long timePerVoxel = measuredTime.nanoTime() / Intervals.numElements(imageDims);
		System.out.println("Total time per voxel " + timePerVoxel + " ns");
		System.out.println("Total time per pixel " + timePerVoxel * cellSize / 1000 + " us");
	}

	private static CachedCellImg<UnsignedShortType, ?> createCellImage(
		CellLoader<UnsignedShortType> loader, long[] imageSize, int[] cellSize)
	{
		ReadOnlyCachedCellImgFactory factory = new ReadOnlyCachedCellImgFactory(
			new ReadOnlyCachedCellImgOptions().cellDimensions(cellSize));
		return factory.create(imageSize, new UnsignedShortType(), loader);
	}
}
