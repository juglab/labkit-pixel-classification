
package net.imglib2.trainable_segmention.pixel_feature.filter.stats;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.util.Cast;
import preview.net.imglib2.algorithm.convolution.Convolution;

import java.util.function.BiConsumer;

public class PixelWiseOperation<T> implements Convolution<T> {

	private final BiConsumer<T, T> operation;

	public PixelWiseOperation(BiConsumer<T, T> operation) {
		this.operation = operation;
	}

	@Override
	public Interval requiredSourceInterval(Interval targetInterval) {
		return targetInterval;
	}

	@Override
	public T preferredSourceType(T targetType) {
		return targetType;
	}

	@Override
	public void process(RandomAccessible<? extends T> source,
		RandomAccessibleInterval<? extends T> target)
	{
		RandomAccessibleInterval<T> sourceT = Cast.unchecked(source);
		RandomAccessibleInterval<T> targetT = Cast.unchecked(target);
		LoopBuilder.setImages(sourceT, targetT).forEachPixel(operation);
	}
}
