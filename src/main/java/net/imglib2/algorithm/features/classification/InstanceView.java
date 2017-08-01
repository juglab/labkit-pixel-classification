package net.imglib2.algorithm.features.classification;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.Features;
import net.imglib2.converter.AbstractConvertedRandomAccess;
import net.imglib2.converter.AbstractConvertedRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import weka.core.Attribute;
import weka.core.Instance;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstanceView<C extends Composite<? extends RealType<?>>> extends AbstractConvertedRandomAccessible< C, Instance >
{

	final RandomAccessible<C> source;

	private final Attribute[] attributes;

	public InstanceView(final RandomAccessible<C> source, final Attribute[] attributes )
	{
		super( source );
		this.source = source;
		this.attributes = attributes;
	}

	static RandomAccessibleInterval<Instance> wrap(Feature feature, List<String> classes, RandomAccessibleInterval<FloatType> featureValues) {
		return wrapComposite(feature, classes, Views.collapseReal(featureValues));
	}

	static <C extends Composite<? extends RealType<?>>> RandomAccessibleInterval<Instance> wrapComposite(Feature feature, List<String> classes, RandomAccessibleInterval<C> collapsed) {
		return Views.interval(new InstanceView<>(collapsed, attributesAsArray(feature, classes)), collapsed);
	}

	private static Attribute[] attributesAsArray(Feature feature, List<String> classes) {
		List<Attribute> attributes = Features.attributes(feature, classes);
		return attributes.toArray(new Attribute[attributes.size()]);
	}

	@Override
	public InstanceAccess<C> randomAccess() {
		return new InstanceAccess<>( source.randomAccess(), attributes );
	}

	@Override
	public InstanceAccess<C> randomAccess(final Interval interval )
	{
		return randomAccess();
	}

	public static class InstanceAccess<C extends Composite<? extends RealType<?>>> extends AbstractConvertedRandomAccess< C, Instance >
	{

		private final CompositeInstance instance;

		private final Attribute[] attributes;


		public InstanceAccess(final RandomAccess< C > source, final Attribute[] attributes )
		{
			super( source );
			instance = new CompositeInstance( source.get(), attributes );
			this.attributes = attributes;
		}

		@Override
		public Instance get()
		{
			instance.setSource( source.get() );
			return instance;
		}

		@Override
		public InstanceAccess< C > copy()
		{
			return new InstanceAccess<>( source.copyRandomAccess(), attributes );
		}

	}

	// length of array is nFeatures + 1 ( for class attribute )
	// all attributes numeric, except class attribute (nominal)
	public static Attribute[] makeDefaultAttributes(final int nFeatures, final int nClasses )
	{
		final Attribute[] attributes = new Attribute[ nFeatures + 1 ];
		for ( int i = 0; i < nFeatures; ++i )
			attributes[ i ] = new Attribute( "" + i );
		attributes[ nFeatures ] = new Attribute( "class", IntStream.range( 0, nClasses ).mapToObj(i -> "" + i ).collect( Collectors.toList() ) );
		return attributes;
	}


}
