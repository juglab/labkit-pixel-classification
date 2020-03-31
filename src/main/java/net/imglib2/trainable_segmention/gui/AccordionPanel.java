package net.imglib2.trainable_segmention.gui;

import java.awt.Color;

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

	public AccordionPanel() {
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		setFocusable( false );
		setBackground(Color.WHITE);
	}

	public void addSection( AccordionSection newSection) {
		add( newSection );
	}
}
