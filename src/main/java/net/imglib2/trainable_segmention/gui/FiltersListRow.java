package net.imglib2.trainable_segmention.gui;

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
	private static final ImageIcon INFO_ICON = new ImageIcon( FiltersListRow.class.getClassLoader().getResource( "info_icon_20px.png" ) );

	private ValuePair<Class<? extends FeatureOp>, String> feature;
	private JCheckBox checkbox;
	private JLabel infoLabel;
	private JLabel nameLabel;
	private boolean isParametrized = false;

	public FiltersListRow( ValuePair<Class<? extends FeatureOp>, String> feature, boolean isParametrized ) {
		this.feature = feature;
		this.isParametrized = isParametrized;

		setLayout( new FlowLayout( FlowLayout.LEFT ) );
		checkbox = new JCheckBox();
		add( checkbox );

		infoLabel = new JLabel( INFO_ICON );
		add( infoLabel );

		nameLabel = new JLabel(toString());
		nameLabel.setVisible( true );
		add( nameLabel);
	}

	public JCheckBox getCheckBox() {
		return checkbox;
	}

	public JLabel getInfoLabel() {
		return infoLabel;
	}

	public JLabel getNameLabel() {
		return nameLabel;
	}
	
	public boolean isSelected() {
		return checkbox.isSelected();
	}

	public FeatureSetting getFeatureSetting() {
		return FeatureSetting.fromClass(feature.getA());
	}
	
	public ValuePair<Class<? extends FeatureOp>, String> getFeature() {
		return feature;
	}

	public boolean isParametrized() {
		return isParametrized;
	}
	
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner( ", " );
		for ( String parameter : getFeatureSetting().parameters() )
			joiner.add( parameter + " = " + getFeatureSetting().getParameter( parameter ) );
		return getFeatureSetting().getName() + " " + joiner;
	}
}
