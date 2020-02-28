package net.imglib2.trainable_segmention.gui;

import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FiltersListRow extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon INFO_ICON = new ImageIcon( FiltersListRow.class.getClassLoader().getResource( "info_icon_20px.png" ) );

	private JCheckBox checkbox;
	private JLabel infoLabel;
	private JLabel nameLabel;

	public FiltersListRow( String name ) {

		setLayout( new FlowLayout( FlowLayout.LEFT ) );
		checkbox = new JCheckBox();
		add( checkbox );

		infoLabel = new JLabel( INFO_ICON );
		add( infoLabel );

		nameLabel = new JLabel();
		add( new JLabel( name ) );
	}

	protected JCheckBox getCheckBox() {
		return checkbox;
	}

	protected JLabel getInfoLabel() {
		return infoLabel;
	}

	protected JLabel getNameLabel() {
		return nameLabel;
	}
}
