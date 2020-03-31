package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.scijava.Context;

import net.imglib2.trainable_segmention.gui.icons.IconResources;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

public class ParametrizedRow extends JPanel implements SelectableRow {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon CUBE_ICON = IconResources.getIcon( "cube_icon_16px.png" );
	private static final ImageIcon DUP_ICON = IconResources.getIcon( "plus_icon_16px.png" );
	private static final ImageIcon INFO_ICON = IconResources.getIcon( "info_icon_16px.png" );

	private GlobalSettings globalSettings;
	private Context context;

	private FeatureSetting featureSetting;

	public ParametrizedRow( Context context, GlobalSettings globalSettings, FeatureSetting featureSetting ) {
		this.context = context;
		this.globalSettings = globalSettings;
		this.featureSetting = featureSetting;
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		setBackground( Color.WHITE );
		initUI();
	}

	private void initUI() {
		JPanel titleRow = new JPanel();
		titleRow.setLayout( new BorderLayout() );
		titleRow.setBackground( Color.WHITE );

		JLabel titleLabel = new JLabel( CUBE_ICON );
		titleLabel.setText( featureSetting.getName() );
		titleLabel.setBorder( new EmptyBorder( 0, 4, 0, 0 ) );
		titleRow.add( titleLabel, BorderLayout.WEST );

		JPanel btnPanel = new JPanel();
		btnPanel.setBackground( Color.WHITE );
		btnPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		JButton dupButton = new JButton( DUP_ICON );
		dupButton.setToolTipText( "Duplicate filter" );
		dupButton.addActionListener( this::duplicate );
		btnPanel.add( dupButton );

		JButton infoButton = new JButton( INFO_ICON );
		infoButton.setToolTipText( "Filter information" );
		infoButton.addActionListener( this::showInfoDialog );
		btnPanel.add( infoButton );
		titleRow.add( btnPanel, BorderLayout.EAST );
		add( titleRow );
		add( new ParametersRow( context, globalSettings, featureSetting, true ) );
	}

	private void showInfoDialog( ActionEvent e ) {
		InfoDialog docoDiag = new InfoDialog( this, "example", "If you use this filter you will do great things" );
		docoDiag.setVisible( true );
	}

	private void duplicate( ActionEvent e ) {
		add( new ParametersRow( context, globalSettings, FeatureSetting.copy( featureSetting ), false ) );
		getParent().getParent().getParent().revalidate();
		getParent().getParent().getParent().repaint();
	}

	public List< FeatureSetting > getSelected() {
		return null;
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
		Component[] children = getComponents(); 
		for( Component child: children) {
			if (child instanceof ParametersRow && ( ( ParametersRow ) child ).isSelected())
				selected.add( ( ( ParametersRow ) child ).getFeatureSetting());
		}
		return selected;
	}
}
