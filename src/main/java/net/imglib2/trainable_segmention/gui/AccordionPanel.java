package net.imglib2.trainable_segmention.gui;

import net.miginfocom.swing.MigLayout;

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

	public AccordionPanel() {
		setLayout( new MigLayout("", "[grow]", ""));
		setFocusable( false );
	}

	public void addSection( AccordionSection newSection) {
		add( newSection, "grow, wrap" );
	}
}
