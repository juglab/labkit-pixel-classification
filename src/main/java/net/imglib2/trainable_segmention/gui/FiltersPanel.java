package net.imglib2.trainable_segmention.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.imglib2.trainable_segmention.pixel_feature.filter.dog2.DifferenceOfGaussiansFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gauss.GaussianBlurFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.GaussianGradientMagnitudeFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.HessianEigenvaluesFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.identity.IdendityFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.laplacian.LaplacianOfGaussianFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.stats.StatisticsFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.structure.StructureTensorEigenvaluesFeature;
import org.scijava.Context;

import net.imglib2.trainable_segmention.gui.FeatureSettingsUI.GlobalsPanel;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

public class FiltersPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private List<FeatureSetting> oldFilters;
	private List<FeatureSetting> grpFilters;
	private List<FeatureSetting> prmFilters;

	public FiltersPanel( Context context, FeatureSettings fs, GlobalsPanel gb ) {
		setLayout(new BoxLayout( this, BoxLayout.Y_AXIS ));
		setBackground( Color.WHITE );
		init( context, AvailableFeatures.getValidFeatures( context, fs.globals() ), gb );
	}

	private void init( Context context, List< FeatureInfo > features, GlobalsPanel gb ) {
		grpFilters = new ArrayList<>();
		prmFilters = new ArrayList<>();
		oldFilters = new ArrayList<>();
		features.sort( Comparator.comparing( FeatureInfo::label ) );
		for(FeatureInfo feature : features) {
			FeatureSetting fs = FeatureSetting.fromClass( feature.clazz() );
			if ( feature.isDeprecated() )
				oldFilters.add(fs);
			else if ( isBasic( feature.clazz() ) )
				grpFilters.add(fs);
			else
				prmFilters.add(fs);
		}

		AccordionPanel accordion = new AccordionPanel();
		accordion.addSection( new FiltersListSection("Basic Filters", context, gb.get(), grpFilters, true ) );
		accordion.addSection( new FiltersListSection("Additional Filters", context, gb.get(), prmFilters, false ) );
		accordion.addSection( new FiltersListSection("Deprecated Filters", context, gb.get(), oldFilters, false ) );
		add(accordion);
	}

	Set<Class<?>> BASIC_FILTERS = new HashSet<>(Arrays.asList(IdendityFeature.class, GaussianBlurFeature.class,
			DifferenceOfGaussiansFeature.class,
			GaussianGradientMagnitudeFeature.class,
			LaplacianOfGaussianFeature.class,
			HessianEigenvaluesFeature.class,
			StructureTensorEigenvaluesFeature.class,
			StatisticsFeature.class));

	private boolean isBasic(Class<? extends FeatureOp> clazz) {
		return BASIC_FILTERS.contains(clazz);
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

		Collection< Class< ? extends FeatureOp > > availableFeatures = AvailableFeatures.getValidFeatures( context, globalSettings ).stream().map( FeatureInfo::clazz ).collect( Collectors.toSet() );

		// Check new features
		List< FeatureSetting > newGrpInvalid = grpFilters.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
		
		List< FeatureSetting > newPrmInvalid = prmFilters.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );

		List< FeatureSetting > oldInvalid = oldFilters.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
		List< FeatureSetting > allInvalid = new ArrayList<>( oldInvalid );
		allInvalid.addAll( newGrpInvalid );
		allInvalid.addAll( newPrmInvalid );
		if ( !allInvalid.isEmpty() )
			showWarning( allInvalid );
		grpFilters.removeAll( newGrpInvalid );
		prmFilters.removeAll( newPrmInvalid );
		oldFilters.removeAll( oldInvalid );
	}
	
	public List<FeatureSetting> getSelectedFeatureSettings() {
		List< FeatureSetting > featureSettings = new ArrayList<>(grpFilters);
		featureSettings.addAll( prmFilters );
		featureSettings.addAll( oldFilters );
		return featureSettings;
	}
}
