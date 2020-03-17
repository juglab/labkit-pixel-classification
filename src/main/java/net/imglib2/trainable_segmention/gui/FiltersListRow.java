package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.StringJoiner;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.util.ValuePair;

public class FiltersListRow extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon INFO_ICON = new ImageIcon( FiltersListRow.class.getClassLoader().getResource( "info_icon_16px.png" ) );
	private static final ImageIcon DUP_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "plus_icon_16px.png" ) );
	private static final ImageIcon RM_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "minus_icon_16px.png" ) );
	private static final ImageIcon PARAMS_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "params_icon_16px.png" ) );

	private ValuePair< Class< ? extends FeatureOp >, String > feature;
	private boolean isParametrized = false;

	private JCheckBox checkbox;
	private JLabel infoButton;
	private JLabel rmButton;
	private JLabel dupButton;
	private JLabel editButton;
	private JLabel nameLabel;

	public FiltersListRow( ValuePair< Class< ? extends FeatureOp >, String > feature, boolean isParametrized ) {
		this.feature = feature;
		this.isParametrized = isParametrized;
		initUI();

	}

	private void initUI() {
		setLayout( new BorderLayout() );

		checkbox = new JCheckBox();
		add( checkbox, BorderLayout.LINE_START );

		nameLabel = new JLabel(toString());
		add( nameLabel, BorderLayout.CENTER );

		JPanel btnPanel = new JPanel();
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
		return FeatureSetting.fromClass( feature.getA() );
	}

	public ValuePair< Class< ? extends FeatureOp >, String > getFeature() {
		return feature;
	}

	public boolean isParametrized() {
		return isParametrized;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner( "," );
		for ( String parameter : getFeatureSetting().parameters() )
			joiner.add( parameter + "=" + getFeatureSetting().getParameter( parameter ) );
		return "<html><b>" + getFeatureSetting().getName() + "</b><br>" + joiner + "</html>";
	}

	public void update() {
		nameLabel.setText( toString() );
		nameLabel.repaint();
	}
}
