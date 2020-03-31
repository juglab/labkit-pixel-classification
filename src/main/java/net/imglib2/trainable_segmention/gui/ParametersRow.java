package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.StringJoiner;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.module.Module;
import org.scijava.module.ModuleCanceledException;
import org.scijava.module.ModuleException;
import org.scijava.ui.swing.widget.SwingInputHarvester;
import org.scijava.widget.InputHarvester;

import net.imglib2.trainable_segmention.gui.icons.IconResources;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

public class ParametersRow extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon RM_ICON = IconResources.getIcon( "minus_icon_16px.png" );
	private static final ImageIcon PARAMS_ICON = IconResources.getIcon( "params_icon_16px.png" );

	private GlobalSettings globalSettings;
	private Context context;
	private FeatureSetting featureSetting;

	
	private JCheckBox checkbox;
	private JLabel paramsLabel;

	public ParametersRow( Context context, GlobalSettings globalSettings, FeatureSetting featureSetting, boolean isFirstRow) {
		this.context = context;
		this.globalSettings = globalSettings;
		this.featureSetting = featureSetting;
		setLayout( new BorderLayout() );
		setBackground( Color.WHITE );
		initUI(isFirstRow);
	}

	private void initUI(boolean isFirstRow) {

		JPanel cbPanel = new JPanel();
		cbPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
		cbPanel.setBackground( Color.WHITE );
		cbPanel.add( Box.createHorizontalStrut( 5 ) );
		checkbox = new JCheckBox();
		cbPanel.add( checkbox );
		add( cbPanel, BorderLayout.WEST );

		paramsLabel = new JLabel( paramsString() );
		add( paramsLabel, BorderLayout.CENTER );

		JPanel btnPanel = new JPanel();
		btnPanel.setBackground( Color.WHITE );
		btnPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		JButton rmButton = new JButton( RM_ICON );
		rmButton.setToolTipText( "Remove filter" );
		rmButton.addActionListener( this::remove );
		if (isFirstRow) 
			rmButton.setEnabled(false);
		btnPanel.add( rmButton );

		JButton editButton = new JButton( PARAMS_ICON );
		editButton.setToolTipText( "Edit filter parameters" );
		editButton.addActionListener( this::editParameters );
		btnPanel.add( editButton );
		add( btnPanel, BorderLayout.EAST );
	}

	private void editParameters( ActionEvent e ) {
		featureSetting = new FeatureSettingsDialog( context ).show( featureSetting, globalSettings );
		update();
		validate();
		repaint();
	}

	private void update() {
		paramsLabel.setText( paramsString() );
		paramsLabel.repaint();
	}

	private void remove( ActionEvent e ) {
		getParent().remove( this );
	}

	private String paramsString() {
		StringJoiner joiner = new StringJoiner( "," );
		for ( String parameter : featureSetting.parameters() )
			joiner.add( parameter + "=" + featureSetting.getParameter( parameter ) );
		String s = joiner.toString();
		s = s.replace( "sigma", "\u03c3" );
		s = s.replace( "psi", "\u03c8" );
		s = s.replace( "gamma", "\u03b3" );
		return "<html>" + s + "</html>";
	}

	public boolean isSelected() {
		return checkbox.isSelected();
	}

	public FeatureSetting getFeatureSetting() {
		return featureSetting;
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
