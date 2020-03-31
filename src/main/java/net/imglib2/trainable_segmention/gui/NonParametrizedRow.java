package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;

public class NonParametrizedRow extends JPanel implements SelectableRow {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon INFO_ICON = IconResources.getIcon( "info_icon_16px.png" );

	private FeatureSetting featureSetting;

	private JCheckBox checkbox;

	public NonParametrizedRow(FeatureSetting featureSetting) {
		this.featureSetting = featureSetting;
		setLayout( new BorderLayout() );
		setBackground(Color.WHITE);
		initUI();
	}

	private void initUI() {

		checkbox = new JCheckBox(featureSetting.getName() );
		add( checkbox, BorderLayout.CENTER );

		JPanel btnPanel = new JPanel();
		btnPanel.setBackground(Color.WHITE);
		btnPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		JButton infoButton = new JButton( INFO_ICON );
		infoButton.setToolTipText( "Filter information" );
		infoButton.addActionListener( this::showInfoDialog );
		btnPanel.add( infoButton );
		add( btnPanel, BorderLayout.LINE_END );
	}
	
	private void showInfoDialog(ActionEvent e) {
		InfoDialog docoDiag = new InfoDialog( this, "example", "If you use this filter you will do great things" );
		docoDiag.setVisible( true );
	}

	public FeatureSetting getFeatureSetting() {
		return featureSetting;
	}

	@Override
	public List< FeatureSetting > getSelectedFeatureSettings() {
		List<FeatureSetting> selected = new ArrayList<>();
		if (checkbox.isSelected())
			selected.add(featureSetting);
		return selected;
	}
}
