package net.imglib2.trainable_segmention.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
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
	private boolean confirmRemove = true;

	public AccordionPanel() {
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
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
	
	public void addSection( AS newSection) {
		add( newSection );
		sections.add( newSection );
		configureNewSection( newSection);
	}

	public void removeSection( AS section ) {
		if ( this.confirmRemove ) {
			int confirm = JOptionPane.showConfirmDialog( this, getRemoveConfirmationMessage(), null, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE );
			if ( confirm != JOptionPane.YES_OPTION ) { return; }
		}
		sections.remove( section );
		remove( section );
		sectionRemoved( section );
		revalidate();
		repaint();
	}

	protected String getRemoveConfirmationMessage() {
		return "Are you sure you want to remove this section?";
	}

	protected void sectionRemoved( AS section ) {
		//Default does nothing
	}

	private void configureNewSection( AS newSection) {
		newSection.setOwner( this );
		setSectionExpanded( newSection );
		revalidate();
		repaint();
	}

	protected List< AS > getSections() {
		return Collections.unmodifiableList( sections );
	}
}