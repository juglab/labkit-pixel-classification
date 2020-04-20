
package preview.net.imglib2.converter;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class RealTypeConverters {

	public static void copyFromTo(RandomAccessible<? extends RealType<?>> source,
		RandomAccessibleInterval<? extends RealType<?>> target)
	{
		// Uses mulrithreaded LoopBuilder
		IntervalView<? extends RealType<?>> sourceInterval = Views.interval(source, target);
		RealType<?> s = Util.getTypeFromInterval(sourceInterval);
		RealType<?> d = Util.getTypeFromInterval(target);
		Converter<RealType<?>, RealType<?>> copy = net.imglib2.converter.RealTypeConverters
			.getConverter(s, d);
		LoopBuilder.setImages(sourceInterval, target).multiThreaded().forEachPixel(copy::convert);
	}

}
