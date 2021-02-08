package net.imglib2.trainable_segmentation.utils.views;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.composite.Composite;

public class FastViews {

	public static < T > RandomAccessibleInterval< Composite< T > > collapse(
			RandomAccessibleInterval< T > image )
	{
		Interval interval = Intervals.hyperSlice(image, image.numDimensions() - 1);
		RandomAccess< Composite< T > > randomAccess =
				CompositeRandomAccess.create(image.randomAccess());
		return SimpleRAI.create( randomAccess, interval );
	}
}
