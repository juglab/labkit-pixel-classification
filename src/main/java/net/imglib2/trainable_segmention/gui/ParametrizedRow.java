package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.scijava.Context;

import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

public class ParametrizedRow extends JPanel implements SelectableRow {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon DUP_ICON = IconResources.getIcon( "plus_icon_16px.png" );
	private static final ImageIcon INFO_ICON = IconResources.getIcon( "info_icon_16px.png" );

	private GlobalSettings globalSettings;
	private Context context;

	private FeatureSetting featureSetting;
	private JCheckBox checkbox;
	private GridBagConstraints gbc;

	public ParametrizedRow( Context context, GlobalSettings globalSettings, FeatureSetting featureSetting ) {
		this.context = context;
		this.globalSettings = globalSettings;
		this.featureSetting = featureSetting;
		setBackground( Color.WHITE );
		setLayout( new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		initUI();
	}

	private void initUI() {
		JPanel titleRow = new JPanel();
		titleRow.setLayout( new BorderLayout() );
		titleRow.setBackground( Color.WHITE );

		checkbox = new JCheckBox( featureSetting.getName() );
		checkbox.addActionListener( this::checkForParameterRow );
		titleRow.add( checkbox, BorderLayout.WEST );

		JPanel btnPanel = new JPanel();
		btnPanel.setBackground( Color.WHITE );
		btnPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		JButton dupButton = new JButton( DUP_ICON );
		dupButton.setFocusPainted(false);
        dupButton.setMargin(new Insets(0, 0, 0, 0));
        dupButton.setContentAreaFilled(false);
        dupButton.setBorderPainted(false);
        dupButton.setOpaque(false);
		dupButton.setToolTipText( "Duplicate filter" );
		dupButton.addActionListener( this::duplicate );
		btnPanel.add( dupButton );

		JButton infoButton = new JButton( INFO_ICON );
		infoButton.setFocusPainted(false);
        infoButton.setMargin(new Insets(0, 0, 0, 0));
        infoButton.setContentAreaFilled(false);
        infoButton.setBorderPainted(false);
        infoButton.setOpaque(false);
		infoButton.setToolTipText( "Filter information" );
		infoButton.addActionListener( this::showInfoDialog );
		btnPanel.add( infoButton );
		titleRow.add( btnPanel, BorderLayout.EAST );
		add( titleRow, gbc);
		gbc.gridy = GridBagConstraints.RELATIVE;
		add( new ParametersRow( context, globalSettings, featureSetting), gbc );
	}

	private void showInfoDialog( ActionEvent e ) {
		InfoDialog docoDiag = new InfoDialog( this, "example", "If you use this filter you will do great things" );
		docoDiag.setVisible( true );
	}

	private void duplicate( ActionEvent e ) {
		add( new ParametersRow( context, globalSettings, FeatureSetting.copy( featureSetting )), gbc );
		revalidate();
		repaint();
	}
	
	private void checkForParameterRow (ActionEvent e) {
		if (getComponents().length == 1) {
			add( new ParametersRow( context, globalSettings, featureSetting), gbc );
			revalidate();
			repaint();
		}
	}

	@Override
	public void remove( Component c ) {
		super.remove( c );
		revalidate();
		repaint();
	}
	
	@Override
	public List< FeatureSetting > getSelectedFeatureSettings() {
		List<FeatureSetting> selected = new ArrayList<>();
		if (checkbox.isSelected())
		{
			Component[] children = getComponents(); 
			for( Component child: children) {
				if (child instanceof ParametersRow )
					selected.add( ( ( ParametersRow ) child ).getFeatureSetting());
			}
		}
		return selected;
	}
}
