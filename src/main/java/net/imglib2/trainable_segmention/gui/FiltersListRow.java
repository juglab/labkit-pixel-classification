package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.StringJoiner;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.module.Module;
import org.scijava.module.ModuleCanceledException;
import org.scijava.module.ModuleException;
import org.scijava.ui.swing.widget.SwingInputHarvester;
import org.scijava.widget.InputHarvester;

import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

public class FiltersListRow extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon INFO_ICON = new ImageIcon( FiltersListRow.class.getClassLoader().getResource( "info_icon_16px.png" ) );
	private static final ImageIcon DUP_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "plus_icon_16px.png" ) );
	private static final ImageIcon RM_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "minus_icon_16px.png" ) );
	private static final ImageIcon PARAMS_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "params_icon_16px.png" ) );

	private FeatureSetting featureSetting;
	private boolean isParametrized = false;

	private JCheckBox checkbox;
	private JLabel infoButton;
	private JLabel rmButton;
	private JLabel dupButton;
	private JLabel editButton;
	private JLabel nameLabel;

	public FiltersListRow( FeatureSetting featureSetting, boolean isParametrized ) {
		this.featureSetting = featureSetting;
		this.isParametrized = isParametrized;
		setLayout( new BorderLayout() );
		setBorder(new EmptyBorder(1,0,1,0));
		setBackground(Color.WHITE);
		initUI();
	}

	private void initUI() {

		checkbox = new JCheckBox();
		add( checkbox, BorderLayout.LINE_START );

		nameLabel = new JLabel( toString() );
		nameLabel.setBorder( new EmptyBorder(0,4,0,0) );
		add( nameLabel, BorderLayout.CENTER );

		JPanel btnPanel = new JPanel();
		btnPanel.setBackground(Color.WHITE);
		btnPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		editButton = new JLabel( PARAMS_ICON );
		editButton.setToolTipText( "Edit filter parameters" );
		editButton.setEnabled( isParametrized );
		btnPanel.add( editButton );

		dupButton = new JLabel( DUP_ICON );
		dupButton.setToolTipText( "Duplicate filter" );
		dupButton.setEnabled( isParametrized );
		btnPanel.add( dupButton );

		rmButton = new JLabel( RM_ICON );
		rmButton.setToolTipText( "Remove filter" );
		rmButton.setEnabled( isParametrized );
		btnPanel.add( rmButton );

		infoButton = new JLabel( INFO_ICON );
		infoButton.setToolTipText( "Filter information" );
		btnPanel.add( infoButton );
		add( btnPanel, BorderLayout.LINE_END );
	}
	
	public void showInfoDialog() {

		InfoDialog docoDiag = new InfoDialog( this, "example", "If you use this filter you will do great things" );
		docoDiag.setVisible( true );

	}

	public void editParameters( Context context, GlobalSettings globalSettings ) {
		featureSetting = new FeatureSettingsDialog( context ).show( featureSetting, globalSettings );
		update();
		validate();
		repaint();
	}

	public JCheckBox getCheckBox() {
		return checkbox;
	}

	public JLabel getInfoButton() {
		return infoButton;
	}

	public JLabel getEditButton() {
		return editButton;
	}

	public JLabel getRemoveButton() {
		return rmButton;
	}

	public JLabel getDuplicateButton() {
		return dupButton;
	}

	public boolean isSelected() {
		return checkbox.isSelected();
	}

	public FeatureSetting getFeatureSetting() {
		return featureSetting;
	}

	public boolean isParametrized() {
		return isParametrized;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner( "," );
		for ( String parameter : featureSetting.parameters() )
			joiner.add( parameter + "=" + featureSetting.getParameter( parameter ) );
		String s = joiner.toString();
		s = s.replace( "sigma", "\u03c3" );
		s = s.replace( "psi", "\u03c8" );
		s = s.replace( "gamma", "\u03b3" );
		return "<html><b>" + featureSetting.getName() + "</b><br>" + s + "</html>";
	}

	public void update() {
		nameLabel.setText( toString() );
		nameLabel.repaint();
	}

	static class FeatureSettingsDialog extends AbstractContextual {

		@SuppressWarnings( "rawtypes" )
		private final InputHarvester harvester;

		FeatureSettingsDialog( Context context ) {
			harvester = new SwingInputHarvester();
			context.inject( harvester );
		}

		FeatureSetting show( FeatureSetting op, GlobalSettings globalSetting ) {
			try {
				Module module = op.asModule( globalSetting );
				harvester.harvest( module );
				return FeatureSetting.fromModule( module );
			} catch ( ModuleCanceledException e ) {
				return op;
			} catch ( ModuleException e ) {
				throw new RuntimeException( e.getMessage() );
			}
		}
	}

}
