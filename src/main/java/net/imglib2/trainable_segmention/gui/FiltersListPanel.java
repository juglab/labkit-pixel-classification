package net.imglib2.trainable_segmention.gui;

import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

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
		DefaultListModel< JCheckBox > newFiltersListModel = new DefaultListModel<>();
		CheckBoxList newFiltersList = new CheckBoxList( newFiltersListModel );
		DefaultListModel< JCheckBox > oldFiltersListModel = new DefaultListModel<>();
		CheckBoxList oldFiltersList = new CheckBoxList( oldFiltersListModel );
		List< ValuePair< Class< ? extends FeatureOp >, String > > features = AvailableFeatures.getValidFeatures( context, GlobalSettings.default2d().build() );
		features.sort( Comparator.comparing( ValuePair::getB ) );
		features.forEach( featureClassAndLabel -> {
			String s = featureClassAndLabel.getB();
			if ( Character.isUpperCase( s.charAt( 0 ) ) )
				oldFiltersListModel.addElement( new JCheckBox( s ) );
			else
				newFiltersListModel.addElement( new JCheckBox( s ) );
		} );

		FiltersListSection oldSection = new FiltersListSection("Deprecated Filters", oldFiltersList, false);
		addSection( oldSection, true );
		FiltersListSection newSection = new FiltersListSection("Current Filters", newFiltersList, true );
		addSection( newSection, false );
	}

	public static void main( String... args ) {
		Context context = new Context();
		final JFrame frame = new JFrame();
		FiltersListPanel panel = new FiltersListPanel( context, GlobalSettings.default2d().build() );
		frame.getContentPane().add( panel );
		frame.setSize( 400, 400 );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );

	}

}
