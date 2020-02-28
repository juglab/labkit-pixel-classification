package net.imglib2.trainable_segmention.gui;

import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;

import org.scijava.Context;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.util.ValuePair;

public class FiltersListPanel extends AccordionPanel< FiltersListSection > {

	private static final long serialVersionUID = 1L;

	public FiltersListPanel( Context context, GlobalSettings globals ) {
		super();
		init( context, globals );
		revalidate();
	}

	private void init( Context context, GlobalSettings globals ) {
		DefaultListModel< FiltersListRow > newFiltersListModel = new DefaultListModel<>();
		FiltersList newFiltersList = new FiltersList( newFiltersListModel );
		DefaultListModel< FiltersListRow > oldFiltersListModel = new DefaultListModel<>();
		FiltersList oldFiltersList = new FiltersList( oldFiltersListModel );
		List< ValuePair< Class< ? extends FeatureOp >, String > > features = AvailableFeatures.getValidFeatures( context, GlobalSettings.default2d().build() );
		features.sort( Comparator.comparing( ValuePair::getB ) );
		features.forEach( featureClassAndLabel -> {
			String s = featureClassAndLabel.getB();
			if ( Character.isUpperCase( s.charAt( 0 ) ) )
				oldFiltersListModel.addElement( new FiltersListRow( s ) );
			else
				newFiltersListModel.addElement( new FiltersListRow( s ) );
		} );

		FiltersListSection oldSection = new FiltersListSection(this,  "Deprecated Filters", oldFiltersList, false );
		addSection( oldSection, true );
		FiltersListSection newSection = new FiltersListSection(this, "Current Filters", newFiltersList, true );
		addSection( newSection, false );
	}

}
