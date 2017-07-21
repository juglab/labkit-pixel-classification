package net.imglib2.algorithm.features;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.RealComposite;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Enumeration;

public class CompositeInstance< R extends RealType< R >> implements Instance
{

	// Store RealComposite< R > instead of Sampler< RealComposite< R > >
	// This requires a setSource method but for getting attributes is only
	// RealComposite.get instead of Sampler.get.get
	private RealComposite< R > source;

	private final Attribute[] attributes;

	private final int classIndex;


	public CompositeInstance(final RealComposite< R > source, final Attribute[] attributes )
	{
		super();
		this.source = source;
		this.attributes = attributes;
		this.classIndex = attributes.length - 1;
	}

	public void setSource( final RealComposite< R > source )
	{
		this.source = source;
	}

	@Override
	public CompositeInstance< R > copy()
	{
		return new CompositeInstance<>( source.copy(), attributes );
	}

	@Override
	public double value( final int attIndex )
	{
		return source.get( attIndex ).getRealDouble();
	}

	@Override
	public double weight()
	{
		// TODO allow non-unit weights?
		return 1.0;
	}

	@Override
	public int classIndex()
	{
		return classIndex;
	}

	@Override
	public Attribute classAttribute()
	{
		return attributes[ classIndex ];
	}

	// class attribute: What is the space of predicted class, e.g. numeric
	// (regression)

	// for RandomForest prediction: Do I only need Instance.value(int)?
	// Should all attributes be numeric (except class attribute)?

	@Override
	public Attribute attribute(final int index )
	{
		return attributes[ index ];
	}

	@Override
	public int numAttributes()
	{
		return attributes.length;
	}

	@Override
	public int numClasses()
	{
		return attributes[ classIndex ].numValues();
	}

	//
	//
	// anything below I do not need to implement?
	//
	//

	@Override
	public int numValues()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Attribute attributeSparse(final int indexOfIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean classIsMissing()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double classValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Instance copy(final double[] values )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Instances dataset()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAttributeAt( final int position )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<Attribute> enumerateAttributes()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equalHeaders( final Instance inst )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String equalHeadersMsg( final Instance inst )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasMissingValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int index( final int position )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void insertAttributeAt( final int position )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMissing( final int attIndex )
	{
		return false;
	}

	@Override
	public boolean isMissingSparse( final int indexOfIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMissing( final Attribute att )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Instance mergeInstance(final Instance inst )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceMissingValues( final double[] array )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClassMissing()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClassValue( final double value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClassValue( final String value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataset( final Instances instances )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMissing( final int attIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMissing( final Attribute att )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue( final int attIndex, final double value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValueSparse( final int indexOfIndex, final double value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue( final int attIndex, final String value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(final Attribute att, final double value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(final Attribute att, final String value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWeight( final double weight )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Instances relationalValue(final int attIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Instances relationalValue(final Attribute att )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String stringValue( final int attIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String stringValue( final Attribute att )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double[] toDoubleArray()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toStringNoWeight( final int afterDecimalPoint )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toStringNoWeight()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toStringMaxDecimalDigits( final int afterDecimalPoint )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString( final int attIndex, final int afterDecimalPoint )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString( final int attIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(final Attribute att, final int afterDecimalPoint )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString( final Attribute att )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double valueSparse( final int indexOfIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double value( final Attribute att )
	{
		throw new UnsupportedOperationException();
	}
}
