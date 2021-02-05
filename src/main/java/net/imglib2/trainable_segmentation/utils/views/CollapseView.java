package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

public class CollapseView {

	public static RandomAccessibleInterval< VectorType > collapseVector(
			RandomAccessibleInterval< ? extends RealType< ? > > image )
	{
		Interval interval = Intervals.hyperSlice(image, image.numDimensions() - 1);
		RandomAccess< ? extends RealType< ? > > ra = image.randomAccess();
		long length = image.dimension(image.numDimensions() - 1);
		RandomAccess< VectorType > randomAccess = new VectorTypeRandomAccess(ra, (int) length);
		return SimpleRAI.create(randomAccess, interval);
	}
}
