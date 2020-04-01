package net.imglib2.trainable_segmention.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

/**
 * UI Component that contains expandable (accordion) sections
 * 
 * @author turekg
 *
 */
public class AccordionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private GridBagConstraints gbc;

	public AccordionPanel() {
		setLayout( new GridBagLayout());
		setFocusable( false );
		setBackground(Color.WHITE);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
	}

	public void addSection( AccordionSection newSection) {
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.weighty = 0.33;
		add( newSection, gbc );
	}
}