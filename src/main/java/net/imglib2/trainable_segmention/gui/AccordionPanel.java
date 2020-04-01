package net.imglib2.trainable_segmention.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
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
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 0.3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		setFocusable( false );
		setBackground(Color.WHITE);
	}

	public void addSection( AccordionSection newSection) {
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.weighty = 0.3;
		add( newSection, gbc );
	}
}