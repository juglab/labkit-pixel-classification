package net.imglib2.trainable_segmention.gui;

import java.awt.Color;
import java.awt.Component;
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

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

public class FiltersPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Set<Class<?>> BASIC_FILTERS = new HashSet<>(Arrays.asList(IdendityFeature.class, GaussianBlurFeature.class,
	DifferenceOfGaussiansFeature.class,
	GaussianGradientMagnitudeFeature.class,
	LaplacianOfGaussianFeature.class,
	HessianEigenvaluesFeature.class,
	StructureTensorEigenvaluesFeature.class,
	StatisticsFeature.class));
	
	private List<FeatureSetting> oldFilters;
	private List<FeatureSetting> grpFilters;
	private List<FeatureSetting> prmFilters;
	private AccordionPanel accordion;
	private FeatureSettingsUI.GlobalsPanel globalsPanel;
	private Context context;

	public FiltersPanel( Context context, FeatureSettings fs, FeatureSettingsUI.GlobalsPanel globalsPanel) {
		this.context = context;
		this.globalsPanel = globalsPanel;
		setLayout(new BoxLayout( this, BoxLayout.Y_AXIS ));
		setBackground( Color.WHITE );
		initUI(AvailableFeatures.getValidFeatures( context, fs.globals()));
	}

	private void initUI(List< FeatureInfo > featureInfos) {
		populateFeatureSettingArrays(featureInfos);

		accordion = new AccordionPanel();
		add(accordion);
		populateAccordionPanel();
	}
	
	private void populateFeatureSettingArrays(List< FeatureInfo > featureInfos) {
		grpFilters = new ArrayList<>();
		prmFilters = new ArrayList<>();
		oldFilters = new ArrayList<>();
		featureInfos.sort( Comparator.comparing( FeatureInfo::label ) );
		for(FeatureInfo featureInfo : featureInfos) {
			FeatureSetting fs = FeatureSetting.fromClass( featureInfo.clazz() );
			if ( featureInfo.isDeprecated() )
				oldFilters.add(fs);
			else if ( isBasic( featureInfo.clazz() ) )
				grpFilters.add(fs);
			else
				prmFilters.add(fs);
		}
	}
	
	private void populateAccordionPanel() {
		accordion.removeAll();
		accordion.addSection( new FiltersListSection("Basic Filters", context, globalsPanel.get(), grpFilters, true ) );
		accordion.addSection( new FiltersListSection("Additional Filters", context, globalsPanel.get(), prmFilters, false ) );
		accordion.addSection( new FiltersListSection("Deprecated Filters", context, globalsPanel.get(), oldFilters, false ) );
	}

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
	
	public void checkFeatures( GlobalSettings globalSettings, boolean increased) {

		if (increased) {
			Collection< Class< ? extends FeatureOp > > availableFeatures = AvailableFeatures.getValidFeatures( context, globalSettings ).stream().map( FeatureInfo::clazz ).collect( Collectors.toSet() );
			List< FeatureSetting > newGrpInvalid = grpFilters.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
			List< FeatureSetting > allInvalid = new ArrayList<>( newGrpInvalid );
			List< FeatureSetting > newPrmInvalid = prmFilters.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
			allInvalid.addAll( newPrmInvalid );
			List< FeatureSetting > oldInvalid = oldFilters.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
			allInvalid.addAll( oldInvalid );
			if ( !allInvalid.isEmpty() )
				showWarning( allInvalid );
			grpFilters.removeAll( newGrpInvalid );
			prmFilters.removeAll( newPrmInvalid );
			oldFilters.removeAll( oldInvalid );
		
		} else {
			populateFeatureSettingArrays(AvailableFeatures.getValidFeatures( context, globalSettings ));
		}
		populateAccordionPanel();

	}
	
	public List<FeatureSetting> getSelectedFeatureSettings() {
		List< FeatureSetting > selected = new ArrayList<>();
		Component[] children = getComponents(); 
		for( Component child: children) {
			if (child instanceof FiltersListSection)
				selected.addAll( ( ( FiltersListSection ) child ).getSelectedFeatureSettings());
		}
		return selected;
	}
}
