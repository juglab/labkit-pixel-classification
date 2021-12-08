/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

package sc.fiji.labkit.pixel_classification.performance;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import sc.fiji.labkit.pixel_classification.Utils;
import sc.fiji.labkit.pixel_classification.classification.Segmenter;
import sc.fiji.labkit.pixel_classification.gson.GsonUtils;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
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
