
package net.imglib2.trainable_segmention.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.scijava.Context;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.util.ValuePair;
import net.miginfocom.swing.MigLayout;

/**
 * UI to select a list of features and their settings.
 *
 * @author turekg
 */
public class FeatureSettingsUI extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Context context;

	private GlobalsPanel globalsPanel;

	private FiltersListModel newFiltersListModel;

	private FiltersListModel oldFiltersListModel;

	public FeatureSettingsUI( Context context, FeatureSettings fs ) {
		this.context = context;
		initUI( fs );
	}

	public FeatureSettings get() {
		List< FeatureSetting > features = newFiltersListModel.getSelectedFeatureSettings();
		features.addAll( oldFiltersListModel.getSelectedFeatureSettings() );
		final GlobalSettings globalSettings = globalsPanel.get();
		return new FeatureSettings( globalSettings, features );
	}

	private void initUI( FeatureSettings fs ) {
		setLayout( new MigLayout( "insets 0", "[grow]", "[][grow][]" ) );
		globalsPanel = new GlobalsPanel( fs.globals() );
		add( globalsPanel, "wrap" );
		add( new JScrollPane( initAccordionPanel( AvailableFeatures.getValidFeatures( context, fs.globals() ) ) ), "split 2, grow" );
	}

	/*
	 * TODO: 1) A way to check for deprecated filters (for now check for
	 * capitalized names)
	 * 2) A way to check if a filter has parameters (for now assume true always)
	 */
	private AccordionPanel< FiltersListSection > initAccordionPanel( List< ValuePair< Class< ? extends FeatureOp >, String > > features ) {
		AccordionPanel< FiltersListSection > accordionPanel = new AccordionPanel<>();
		newFiltersListModel = new FiltersListModel();
		oldFiltersListModel = new FiltersListModel();
		features.sort( Comparator.comparing( ValuePair::getB ) );
		features.forEach( feature -> {
			String s = feature.getB();
			if ( Character.isUpperCase( s.charAt( 0 ) ) )
				oldFiltersListModel.add( new FiltersListRow( feature, true ) );
			else
				newFiltersListModel.add( new FiltersListRow( feature, true ) );
		} );

		accordionPanel.addSection( new FiltersListSection( accordionPanel, "Deprecated Filters", new FiltersList( context, globalsPanel, oldFiltersListModel ), false ), true );
		accordionPanel.addSection( new FiltersListSection( accordionPanel, "Current Filters", new FiltersList( context, globalsPanel, newFiltersListModel ), true ), false );
		return accordionPanel;
	}

	public class GlobalsPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private final ChannelSetting channelSetting;

		private final JComboBox< String > dimensionsField;

		private final JFormattedTextField sigmasField;

		public GlobalsPanel( GlobalSettings globalSettings ) {
			channelSetting = globalSettings.channelSetting();
			setLayout( new MigLayout( "insets 0", "[]20pt[100pt]", "[][][]" ) );
			add( new JLabel( "Dimensions:" ) );
			dimensionsField = new JComboBox<>( new String[] { "2D", "3D" } );
			dimensionsField.setSelectedItem( globalSettings.numDimensions() + "D" );
			dimensionsField.addActionListener( this::dimensionsChanged );
			add( dimensionsField, "wrap" );
			add( new JLabel( "Sigmas:" ) );
			sigmasField = new JFormattedTextField( new ListOfDoubleFormatter() );
			sigmasField.setValue( globalSettings.sigmas() );
			add( sigmasField, "grow, wrap" );
		}

		@SuppressWarnings( "unchecked" )
		GlobalSettings get() {
			return GlobalSettings.default2d().channels( channelSetting ).dimensions( dimensionsField.getSelectedIndex() + 2 ).sigmas( ( List< Double > ) sigmasField.getValue() ).build();
		}

		private void dimensionsChanged( ActionEvent e ) {
			checkFeatures( get(), context );
		}

		private void checkFeatures( GlobalSettings globalSettings, Context context ) {

			Collection< Class< ? extends FeatureOp > > availableFeatures = AvailableFeatures.getValidFeatures( context, globalSettings ).stream().map( ValuePair::getA ).collect( Collectors.toSet() );

			// Check new features
			List< FeatureSetting > newFeatures = newFiltersListModel.getFeatureSettings();
			List< FeatureSetting > newInvalid = newFeatures.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );

			List< FeatureSetting > oldFeatures = oldFiltersListModel.getFeatureSettings();
			List< FeatureSetting > oldInvalid = oldFeatures.stream().filter( feature -> !availableFeatures.contains( feature.pluginClass() ) ).collect( Collectors.toList() );
			List< FeatureSetting > allInvalid = new ArrayList<>( oldInvalid );
			allInvalid.addAll( newInvalid );
			if ( !allInvalid.isEmpty() )
				showWarning( allInvalid );
			newFiltersListModel.removeAll( newInvalid );
			oldFiltersListModel.removeAll( oldInvalid );
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

	}

	public static Optional< FeatureSettings > show( Context context, FeatureSettings fg ) {
		FeatureSettingsUI featureSettingsGui = new FeatureSettingsUI( context, fg );
		return featureSettingsGui.showInternal();
	}

	private Optional< FeatureSettings > showInternal() {
		boolean ok = showResizeableOkCancelDialog( "Select Pixel Features", this );
		if ( ok ) {
			FeatureSettings features = get();
			return Optional.of( features );
		} else
			return Optional.empty();
	}

	private static boolean showResizeableOkCancelDialog( String title, JPanel content ) {
		JDialog dialog = new JDialog( ( Frame ) null, title, true );
		JOptionPane optionPane = new JOptionPane( content, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
		dialog.setContentPane( optionPane );
		dialog.setResizable( true );
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		dialog.setPreferredSize( new Dimension( 600, 600 ) );
		optionPane.addPropertyChangeListener( e -> {
			String prop = e.getPropertyName();
			if ( dialog.isVisible() && ( e.getSource() == optionPane ) && ( JOptionPane.VALUE_PROPERTY.equals(
					prop ) ) )
				dialog.dispose();
		} );
		dialog.pack();
		dialog.setVisible( true );
		return optionPane.getValue().equals( JOptionPane.OK_OPTION );
	}

	public static void main( String... args ) {
		Context context = new Context();
		final Optional< FeatureSettings > show = FeatureSettingsUI.show( context, new FeatureSettings( GlobalSettings.default2d().build() ) );
		System.out.println(
				show.map( featureSettings -> featureSettings.toJson().toString() ).orElse(
						"Cancelled" ) );
		System.out.println( "finished" );
	}

	private static class ListOfDoubleFormatter extends JFormattedTextField.AbstractFormatter {

		private static final long serialVersionUID = 1L;

		@Override
		public Object stringToValue( String text ) throws ParseException {
			return Stream.of( text.split( ";" ) ).map( Double::new ).collect( Collectors.toList() );
		}

		@Override
		public String valueToString( Object value ) throws ParseException {
			if ( value == null )
				return "";
			@SuppressWarnings( "unchecked" )
			List< Double > list = ( List< Double > ) value;
			StringJoiner joiner = new StringJoiner( "; " );
			list.stream().map( Object::toString ).forEach( joiner::add );
			return joiner.toString();
		}
	}
}
