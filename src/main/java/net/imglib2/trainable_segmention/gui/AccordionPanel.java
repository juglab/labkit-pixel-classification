package net.imglib2.trainable_segmention.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * UI Component that contains expandable (accordion) sections
 * 
 * @author turekg
 *
 */
public class AccordionPanel< AS extends AccordionSection< AS > > extends JPanel {

	private static final long serialVersionUID = 1L;
	private Set< AS > expandedSections = new HashSet<>();
	private List< AS > sections = new ArrayList<>();

	public AccordionPanel() {
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ));
		setFocusable( false );
	}

	public Set< AS > getExpandedSections() {
		return expandedSections;
	}

	public void setSectionExpanded( AS section ) {
		expandedSections.add( section );
	}

	public void setSectionCollapsed( AS section ) {
		expandedSections.remove( section );
	}

	public void addSection( AS newSection, boolean collapse ) {
		add( newSection );
		sections.add( newSection );
		configureNewSection( newSection, collapse );
	}

	private void configureNewSection( AS newSection, boolean collapse ) {
		newSection.setOwner( this );
		if ( collapse ) {
			setSectionCollapsed(newSection);
		} else {
			setSectionExpanded( newSection );
		}
		revalidate();
		repaint();
	}

	protected List< AS > getSections() {
		return Collections.unmodifiableList( sections );
	}
}
