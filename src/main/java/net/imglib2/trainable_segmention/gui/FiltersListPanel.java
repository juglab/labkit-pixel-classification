package net.imglib2.trainable_segmention.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.scijava.Context;

import net.imglib2.trainable_segmention.gui.FeatureSettingsUI.GlobalsPanel;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.util.ValuePair;

public class FiltersListPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private FiltersListModel oldFiltersListModel;
	private FiltersListModel grpFiltersListModel;
	private FiltersListModel prmFiltersListModel;

	public FiltersListPanel( Context context, FeatureSettings fs, GlobalsPanel gb ) {
		setLayout(new BoxLayout( this, BoxLayout.Y_AXIS ));
		setBackground( Color.WHITE );
		init( context, AvailableFeatures.getValidFeatures( context, fs.globals() ), gb );
	}

	private void init( Context context, List< ValuePair< Class< ? extends FeatureOp >, String > > features, GlobalsPanel gb ) {
		grpFiltersListModel = new FiltersListModel();
		prmFiltersListModel = new FiltersListModel();
		oldFiltersListModel = new FiltersListModel();
		features.sort( Comparator.comparing( ValuePair::getB ) );
		features.forEach( feature -> {
			String s = feature.getB();
			FeatureSetting fs = FeatureSetting.fromClass( feature.getA() );
			boolean prms = isParametrized( feature.getA() );
			if ( Character.isUpperCase( s.charAt( 0 ) ) ) {
				if ( prms )
					prmFiltersListModel.add( new FiltersListRow( fs, true ) );
				else
					grpFiltersListModel.add( new FiltersListRow( fs, false ) );
			} else {
				oldFiltersListModel.add( new FiltersListRow( fs, prms ) );
			}
		} );

		AccordionPanel<FiltersListSection> grpPanel = new AccordionPanel<>();
		grpPanel.addSection( new FiltersListSection( grpPanel, "Group Filters", new FiltersList( context, gb, grpFiltersListModel ), true ) );
		add(grpPanel);
		AccordionPanel<FiltersListSection> prmPanel = new AccordionPanel<>();
		prmPanel.addSection( new FiltersListSection( prmPanel, "Parametrized Filters", new FiltersList( context, gb, prmFiltersListModel ), true ) );
		add(prmPanel);
		AccordionPanel<FiltersListSection> oldPanel = new AccordionPanel<>();
		oldPanel.addSection( new FiltersListSection( oldPanel, "Deprecated Filters", new FiltersList( context, gb, oldFiltersListModel ), false ) );
		add(oldPanel);
	}

	private boolean isParametrized( Class< ? extends FeatureOp > featureClass ) {
		Set< String > params = FeatureSetting.fromClass( featureClass ).parameters();
		return !params.isEmpty();
	}
	
	private void showWarning( List< FeatureSetting > invalid ) {
		final StringBuilder text = new StringBuilder( "The following features need to be removed because they don't fit the global settings:" );
		invalid.forEach( feature -> text.append( "\n* " ).append( toString( feature ) ) );
		JOptionPane.showMessageDialog(
				null,
				text.toString(),
				"Feature Settings",
				JOptionPane.WARNING_MESSAGE );
	}
	
	private String toString( FeatureSetting feature ) {
		StringJoiner joiner = new StringJoiner( ", " );
		for ( String parameter : feature.parameters() )
			joiner.add( parameter + " = " + feature.getParameter( parameter ) );
		return feature.getName() + " " + joiner;
	}
	
	public void checkFeatures( GlobalSettings globalSettings, Context context ) {

		Collection< Class< ? extends FeatureOp > > availableFeatures = AvailableFeatures.getValidFeatures( context, globalSettings ).stream().map( ValuePair::getA ).collect( Collectors.toSet() );

		// Check new features
		List< FeatureSetting > newGrpFeatures = grpFiltersListModel.getFeatureSettings();
		List< FeatureSetting > newGrpInvalid = newGrpFeatures.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
		
		List< FeatureSetting > newPrmFeatures = prmFiltersListModel.getFeatureSettings();
		List< FeatureSetting > newPrmInvalid = newPrmFeatures.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );

		List< FeatureSetting > oldFeatures = oldFiltersListModel.getFeatureSettings();
		List< FeatureSetting > oldInvalid = oldFeatures.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
		List< FeatureSetting > allInvalid = new ArrayList<>( oldInvalid );
		allInvalid.addAll( newGrpInvalid );
		allInvalid.addAll( newPrmInvalid );
		if ( !allInvalid.isEmpty() )
			showWarning( allInvalid );
		grpFiltersListModel.removeAll( newGrpInvalid );
		prmFiltersListModel.removeAll( newPrmInvalid );
		oldFiltersListModel.removeAll( oldInvalid );
	}
	
	public List<FeatureSetting> getSelectedFeatureSettings() {
		List< FeatureSetting > featureSettings = grpFiltersListModel.getSelectedFeatureSettings();
		featureSettings.addAll( prmFiltersListModel.getSelectedFeatureSettings() );
		featureSettings.addAll( oldFiltersListModel.getSelectedFeatureSettings() );
		return featureSettings;
	}
}
